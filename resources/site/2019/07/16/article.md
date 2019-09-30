For the last four years, I've been working at Datastax on the [OpsCenter][opscenter] product team. [OpsCenter][opscenter] 
itself was originally built with [Twisted][twisted] on python and leveraged small agents, written in Clojure, to interact 
with every Cassandra node instance. Shortly after I started, the core team decided to port the python component to 
[Jython][jython], a JVM based Python implementation. This isn't a secret or propitiatory information since our 
architect, [Nick Bailey][nb], 
gave a talk at Clojure/conj 2016 about our efforts.

<iframe width="560" height="315" src="https://www.youtube.com/embed/wfrajaEyNX0" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

The eventual goal by moving to [Jython][jython] was that all of the legacy [Twisted][twisted] application code was now 
on the JVM and that it could interop with other JVM code, namely Clojure. This would allow us to slowly rewrite 
functional sections of the application in Clojure instead of the need to have a big bang style rewrite effort. At a high 
level, this sounds great. [Jython][jython] clearly publishes documentation on how easy it is to interop with Java 
classes/objects from Python. Clojure does the same. But making two dynamic languages on the JVM that don't generate 
classes/object bytecode interop? That's not as easy.  Both Clojure and Jython implement a Java API for manipulating the 
runtime. For instance, Clojure functions can be used in Java but require understanding the Java API.

```java
import clojure.java.api.Clojure;

public class MainClass {
    public static void main(final String[] args) throws Exception {
        IFn plusFn = Clojure.var("clojure.core", "+");
        Object result = plusFn.invoke(1, 2);
        // result is now 1 + 2 as preformed by Clojure
    }
}
```

The same holds true with [Jython][jython]. When trying to load any arbitrary Python code with the Jython runtime, you end up
getting instances of `PyObject` or `PyFunction` which is an abstraction on the concrete code. In both cases, the 
API exposed ends up being a generic `invoke` or `__call__`  method on the abstract object used to invoke the code. This
makes calling between the two ugly and often involves a lot of boilerplate code. Fortunately, [Clojure's metaprogramming][clojure-metaprogramming]
facilities are powerful and we can use them to build a small interop library that makes code form [Jython][jython] look like
it's just Clojure code.


## The Goal

Assume for a moment that I have some simple python code in the `myapp/legacy.py` file:

```python
def pi_value():
   return 3.1415
   
def circ(radius):
   return 2 * pi_value() * radius
   
def factorial(n):
   result = 1
   for i in range(n, 1, -1):
      result *= i
   return result
```

Using these functions from Clojure shouldn't require a deep knowledge of [Jython][jython] or the special double underscore methods
in python. In fact, I'd like to be able to write something that just looks like Clojure:

```clojure
(ns myapp.newcode
    (:require [interops.jython :as jy]))
    
(jy/import "myapp.legacy")

(def PI (myapp.legacy/pi_value))   ; => 3.1415

(myapp.legacy/circ 2)              ; => 12.566

(myapp.legacy/factorial 4)         ; => 24
```

In summary, I'd like the `jy/import` function to expose vars in the current namespace that have the same name as their
[Jython][jython] counterparts. These vars, which are function definitions, basically should already have the interop code applied
so I can send Clojure data types in and get Clojure data types out.


## Multimethods for Data Conversion

So one issue we need to deal with is the conversion of data between Clojure and [Jython][jython] types. Clojure leverages Java
built in types whenever possible. This allows us to use a set of helper static methods on the `org.python.core.Py`
class to convert Java types to their corresponding [Jython][jython] types. Dispatch on which static method to use is based on
the Java class so we can use the [`type`][type-fn] function along with a multimethod to make a very declarative set of rules for
converting types.

```clojure
;; Define rules for casing from clojure objects to their jython equivalent. This
;; list is not an exhaustive since it doesn't take clojure maps or sequences
;; into account.
(defmulti cast:clj->py type)
(defmethod cast:clj->py java.lang.Integer  [obj] (Py/newInteger obj))
(defmethod cast:clj->py java.lang.Long     [obj] (Py/newLong obj))
(defmethod cast:clj->py java.lang.Float    [obj] (Py/newFloat obj))
(defmethod cast:clj->py java.lang.Double   [obj] (Py/newFloat obj))
(defmethod cast:clj->py java.lang.String   [obj] (Py/newString obj))
(defmethod cast:clj->py :default           [obj] obj)
```

It turns out this is also helpful since we can add additional dispatch rules later. We don't have to update the specifics
of our conversion code in the core layer if a specific utility script or section of code needs a one off rule. The same
pattern of multimethod dispatch can be used to convert from python types to Clojure types as well.

```clojure
(defmulti cast:py->clj(fn [obj](str (type obj))))
(defmethod cast:py->clj "class org.python.core.PyInteger"    [obj] (.asInt obj))
(defmethod cast:py->clj "class org.python.core.PyLong"       [obj] (.asLong obj))
(defmethod cast:py->clj "class org.python.core.PyFloat"      [obj] (.asDouble obj))
(defmethod cast:py->clj "class org.python.core.PyString"     [obj] (.asString obj))
(defmethod cast:py->clj "class org.python.core.PyUnicode"    [obj] (.asString obj))
(defmethod cast:py->clj "class org.python.core.PyBoolean"    [obj] (Py/py2boolean obj))
(defmethod cast:py->clj :default                             [obj] obj)
```

You'll notice that the conversion from [Jython][jython] to Clojure types needs to dispatch on the string representation of the 
Class object instead of the actual Class object. This is due to a bug in the [Jython][jython] runtime that we hit. Since we're 
avoiding boilerplate code, we can abstract this fix to a single multimethod and perhaps update the implementation when
the Jython bug is fixed.


## Helper Functions

With the data type problem solved, we can focus on some helper functions to make the implementation of higher level
inteop code more readable. Python, and conversely [Jython][jython], depend a lot on magical methods that have a double underscore
prefix and suffix. This is how many of the core functions are implemented. For example, the [`dir`][dir-fn] function returns the
list of attributes and methods on an object but is also implemented on every object as the `__dir__` method. Here are
the helper functions that we have for encapsulating specific python conventions.

```clojure
(defonce sys-module (PySystemState.))
(sys-module)


(defn- getattr
  [obj name]
  (.__getattr__ obj name))


(defn- dir
  [obj]
  (.__dir__ obj))
  
  
(defn- raw-import
  "Uses the Python __import__ function through the Jython API to load a Python
  module from the JYTHONPATH and returns a raw PyModule object that contains a
  nested structure of artifacts that were imported.

  See https://docs.python.org/2.7/library/functions.html#__import__ for more
  details on how to manually do imports in Python."
  [module-name]
  (let [importer-fn-name (cast:clj->py "__import__")
        importer-fn (.. sys-module (getBuiltins) (__getitem__ importer-fn-name))]
    (.__call__ importer-fn (cast:clj->py module-name))))


(defn- is-dunder?
  "Checks to see if a Python object name starts and ends with the double underscore
  pattern, called a dunder. Returns true if the name matches that pattern, otherwise
  returns false."
  [name]
  (and (.startsWith name "__")
       (.endsWith name "__")))


(defn- not-dunder?
  "Inverse check of is-dunder? function."
  [name]
  (not (is-dunder? name)))
``` 


## Wrapping PyFunctions

The next step in our interop is wrapping the call to [Jython's][jython] `__call__` methods in a Clojure function. This
wrapper also needs to convert all the arguments into their [Jython][jython] type and then convert the return values back
to Clojure types. The wrapper also supports calling via keywords like python does using keywords for the argument names.

```clojure
(defn- invoked-with-keywords?
  "Checks to see if the args used to invoke a clojure function look like the
  arguments follow python's keyword argument style. This function does not handle
  mixed argument passing, which should evaluate to false."
  [args]
  (and (even? (count args))
       (keyword? (first args))))
       
       
(defn convert-args
  "Helper function to convert a sequence of clojure arguments into a PyObject
  Java array with each argument converted to the corresponding python type."
  [clj-args]
  (into-array PyObject (map cast:clj->py clj-args)))
  
  
(defn wrap-jython-fn
  "Wraps up a PyFunction object type so that it can called from Clojure like a native
  function. Input arguments can be provided in a purly ordinal fashion or they can be
  keyword specified. Return values are also converted."
  [alien-fn]
  (fn [& args]
    (let [result (if (invoked-with-keywords? args)
                     (.__call__ alien-fn (convert-args (take-nth 2 (rest args)))
                                         (into-array java.lang.String (map name (take-nth 2 args))))
                     (.__call__ alien-fn (convert-args args)))]
      (cast:py->clj result))))
```

One thing that would be nice for repl users would have been injecting any python docstrings from functions and methods
into the wrapper instance to preserve documentation. Since this was for code all within a single project, it wasn't really
a high value feature to work on.


## The Heart of the Interop

Now we have enough of the foundation levels to build out the powerful `import` interop function and the interesting bits
of [metaprogramming][clojure-metaprogramming]. Using Clojure's [`create-ns`][[create-ns-fn]] and [`intern`][intern-fn], 
we can dynamically create a new namespace and then add the wrapped functions to this new namespace. The code is relatively short: 

```clojure
(defn- py-objs-from
  "Given a module reference (PyModule object instance), this function returns
  a sequence of maps containing the :name of the object and the PyObject :ref
  that is the python object in the Jython runtime."
  [module-ref]
  (->> (dir module-ref)
       (filter not-dunder?)
       (map #(hash-map :name (symbol %1) :ref (getattr module-ref %1)))))
       
       
(defn- synthesize-ns!
  "Given a ns-name and a sequence of maps contains object :name and :ref
  values, this method creates a new namespace using ns-name and then defines
  all of the obj-defs by their :name with the :ref casted to be a suitable
  clojure value."
  [ns-name obj-defs]
  (let [ns-sym (symbol ns-name)
        ns-ref (create-ns ns-sym)]
    (doseq [{:keys [name ref]} obj-defs]
        (intern ns-ref name (cast:py->clj ref)))))
        
        
(defn import
  "Low level API for loading a Python module as a clojure namespace
  into the running environment. Any existing namespace known as module-name
  will be overwritten without warning."
  [module-name]
  (let [module-base (raw-import module-name)
        name-parts (rest (string/split module-name #"\."))
        child-module (reduce #(getattr %1 %2) module-base name-parts)
        imported-objs (py-objs-from child-module)]
    (synthesize-ns! module-name imported-objs)))
```

With `import` in place, we can now fulfill our goal of writing Clojure that calls into Jython without any sort of 
boilerplate or hurdles.


## Improvements

As outlined there are two major changes that would be necessary for a fully functional interop library. The `import`
function currently doesn't make any sort of local scoped vars, allow for a shortcut alias or allow you to limit the
things brought in. I would envision either expanding the `import` function or building a `require` function that implements
this more advanced binding logic. Something like:

```clojure
(jy/require ["myapp.legacy" :refer :all]       #_ "like import but also makes local vars"
            ["myapp.legacy" :refer ["circ"]]   #_ "only make circ a local var"
            ["myapp.legacy" :as legacy]        #_ "like import but makes a local var alias named legacy")
```

The second big improvement would be a way to pass a Clojure function into python and allow it to execute seamlessly. 
This was not something specifically that we had a need for but python also supports the higher order function pattern
like Clojure. The difficulty here is either using a bytecode emiiter to define a functional interface class definition
on the fly or building out a solution using the Jython Java API.


## Does it Work?

A more streamlined version of this interop code is running in the [OpsCenter][opscenter] product today but the efforts to port all
of [OpsCenter][opscenter]  to Clojure have been halted. Almost all of the difficulties with this approach have stemmed from the 
fragility of the [Jython][jython] runtime (specifically 2.7.1). The Java API is not well documented and often has some buggy 
behaviors. Other JVM languages, like Scala and Kotlin, already produce class and object artifacts that can be accessable
through Clojure's Java existing interop. As the GraalVM project matures and adds more languages, this becomes less
important.

Yet this kind of approach might have value in building a [GraphQL][graphql] or [gRPC][grpc] client library that can build 
functional bindings at runtime given some kind of IDL or schema.  

[opscenter]: https://www.datastax.com/products/datastax-opscenter
[twisted]: https://twistedmatrix.com
[jython]: https://www.jython.org/
[clojure-metaprogramming]: http://clojure-doc.org/articles/language/macros.html
[nb]: https://twitter.com/nickmbailey
[type-fn]: https://clojuredocs.org/clojure.core/type
[dir-fn]: https://docs.python.org/2/library/functions.html#dir
[create-ns-fn]: https://clojuredocs.org/clojure.core/create-ns
[intern-fn]: https://clojuredocs.org/clojure.core/intern
[graphql]: https://graphql.org/
[grpc]: https://grpc.io/

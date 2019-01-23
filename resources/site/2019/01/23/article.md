

## The bug

Our automated functional tests caught an exception in a block of functionality that needs to read a sequence of strings
from a deeply nested [YAML][yaml] file structure. 

```clojure
(defn load-yaml-contents
  []
  ;; This is a mocked set of data from parsing the YAML file.
  (map identity [["one" "two" "three"] ["four" "five"]]))
  
(-> (load-yaml-contents)
    flatten
    (reduce concat []))
=> (\o \n \e \t \w \o \t \h \r \e \e \f \o \u \r \f \i \v \e)
```

After [`flatten`][flatten-fn] we would have a list of strings but then when that sequence is reduced using [`concat`][concat-fn], 
each string value is treated as a sequence of characters and each character is appended to the vector. This is not the result 
that we want. I then had to question if this code ever worked. There was a unit test around this behavior that asserted the 
result of this code produced a sequence of strings. This unit test was last touched when the code shipped and has been running 
successfully in nightly builds. I was perplexed as to why the test passed but the code failed in an end to end test. 

It turns out that a recent change introduced a wrapper around the [YAML][yaml] parsing that converted [snakeyaml's][snakeyaml] Java types 
into Clojure types. What happens when we mimic Java data structures as a return from `load-yaml-content`? 

```clojure
(defn load-yaml-contents
  []
  ;; This is a mocked set of data from parsing the YAML file.
  (map identity [(doto (java.util.ArrayList.)
                       (.add "one")
                       (.add "two")
                       (.add "three"))
                 (doto (java.util.ArrayList.)
                       (.add "four")
                       (.add "five"))]))
                       
(-> (load-yaml-content)
    flatten
    (reduce concat []))
=> ("one" "two" "three" "four" "five")
```

We can see that the result of processing the nested data structure is a simple sequence of all the data elements. This matches
the value our test suite was asserting and what the production code expected.  Keen readers will start to 
question the [`reduce`][reduce-fn] statement after the [`flatten`][flatten-fn] as a possible redundant statement.  If we remove 
the [`reduce`][reduce-fn] call, we can clearly see that flatten doesn't actually flatten out the nested data structures.

```clojure
(flatten (load-yaml-contents))
=> (["one" "two" "three"] ["four" "five"])
```

Getting the code to run again is as simple as removing the [`reduce`][reduce-fn] or the [`flatten`][flatten-fn] stage in 
the threading macro.


## Why is `flatten` broken?

It does seem like this ia bug in the [`clojure.core/flatten`][flatten-fn] function. Let's take a look at the source:

```clojure
(defn flatten
  "Takes any nested combination of sequential things (lists, vectors,
  etc.) and returns their contents as a single, flat sequence.
  (flatten nil) returns an empty sequence."
  {:added "1.2"
   :static true}
  [x]
  (filter (complement sequential?)
          (rest (tree-seq sequential? seq x))))
``` 

So the implementation of [`flatten`][flatten-fn] only flatten out collections in `x` if those collections implement 
the [`clojure.lang.Sequential`][Sequential] interface. Obviously, Java types produced from Java libraries wouldn't implement a
Clojure specific interface. Complicating this issue is the ambiguous docstring:

>  Takes any nested combination of sequential things (lists, vectors, etc.)

In this case, "sequential" actually refers to "Sequential" (aka "clojure.lang.Sequential") and is not a generic use
of the term to mean any list of sequence of data. It's not actually clear here whether or not the intent was for
[`flatten`][flatten-fn] to work across data that implements [`java.util.List`][java.util.List] or if the intent is to 
restrict the use to only Clojure's native data types. In fact, this particular usage is counter to other built-in function 
implementations like [`map`][map-fn], [`reduce`][reduce-fn], [`filter`][filter-fn], [`into`][into-fn], [`doseq`][doseq-fn], 
[`for`][for-fn], etc. Each of these functions is able to manipulate an [`ArrayList`][java.util.ArrayList] the same way 
that a vector is manipulated.


## Why did the unit test not catch this?

Recall that I mentioned there was a unit test in our code base that was running with nightly builds and asserting that
the code in question was implemented correctly. The answer is simple - the test setup a mock return using 
[`with-redef`][with-redef-fn] that called [snakeyaml][snakeyaml] directly. This meant that new logic added in other parts of the code 
around the [YAML][yaml] parsing was not exercised as part of this test. Not mocking deep enough to fully exercise functionality 
is a common and easy mistake to make.

Another fix to this code included a broader unit test that created a temporary file with the test content. The function
in our code base that returned the [YAML][yaml] file as a [`java.io.File`][java.io.File] instance was redefined to return this 
temporary file. Once that change was made to the code, the unit test started failing and it was then easy enough to make 
corrections to the code and ensure they worked as intended.


## Lessons learned

The first lesson that was (re)learned was about the dangers of mocks in unit tests. They can be invaluable in writing
tests but their overuse or mocking the wrong object can lead to a test that misses cases or simply tests the ability
of the author to create mocks.

The second lesson here is that the Clojure interop story is a bit more fragile than we might like to admit. To avoid odd
issues, data that is contained in Java objects should be repackaged into Clojure native types before being passed around
in a Clojure app. For objects, the [`bean`][bean-fn] function should be used to produce a map of named fields. Lists and 
sets should be transformed with [`into`][into-fn]. This also implies that Java code should be wrapped in thin facade 
functions where this translation can happen.    


[bean-fn]: https://clojuredocs.org/clojure.core/bean
[into-fn]: https://clojuredocs.org/clojure.core/into 
[map-fn]: https://clojuredocs.org/clojure.core/map
[reduce-fn]: https://clojuredocs.org/clojure.core/reduce
[filter-fn]: https://clojuredocs.org/clojure.core/filter
[doseq-fn]: https://clojuredocs.org/clojure.core/doseq
[for-fn]: https://clojuredocs.org/clojure.core/for
[flatten-fn]: https://clojuredocs.org/clojure.core/flatten
[concat-fn]: https://clojuredocs.org/clojure.core/concat
[with-redef-fn]: https://clojuredocs.org/clojure.core/with-redefs
[java.io.File]: https://docs.oracle.com/javase/8/docs/api/java/io/File.html
[Sequential]: https://github.com/clojure/clojure/blob/clojure-1.10.0/src/jvm/clojure/lang/Sequential.java
[yaml]: https://yaml.org/
[snakeyaml]: http://www.snakeyaml.org
[java.util.List]: https://docs.oracle.com/javase/8/docs/api/java/util/List.html
[java.util.ArrayList]: https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html
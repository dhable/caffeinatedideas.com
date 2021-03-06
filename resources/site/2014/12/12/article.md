A recent student posed the question, "Is there a way to lock execution of an entire
method in Python using Lock instead of writing out a with-block?" My answer
started with all the good reasons why we wouldn't want to hide the locking behavior
in magic, why adding it to all methods on all objects in python would be bad for
performance and testing and then to argue that the current syntax based on the with
keyword isn't that painful. Then I tried to see if I could replicate the problem
using some python magic.

### Java-like 'synchronized' Construct

What the student was really looking for was something similar to the synchronized keyword
in the Java language. If you aren't familiar with Java, every object instance has a
mutex that can be locked by a single thread. When you add the synchronized keyword in a
method signature or a synchronized block, Java adds the code to acquire the lock on
the object currently bound to `this`.  The end result is that the developer can ensure
a method is thread safe simply by stating:

```java
public class LinkedList {
    public synchronized void insert(final Object data) {
        // ...
    }
}
```

To replicate this behavior in python, we'd need two extra pieces of functionality.

1. Python objects do not implicitly contain a mutex lock, so we need to allocate
an instance of `threading.Lock` on self during object construction.

2. We need to acquire this implicit Lock when the method starts and then release
the Lock whenever flow exits our methods.

The equivalent python code would be:

```python
class LinkedList:
   def__init__(self):
      self._lock = threading.Lock()

   def insert(self):
      with self._lock:
         # ...
         pass
```

Let's see if we can cut down that python code.

### Decorators

My first thought was to define a decorator, called `synchronized` that would
return a new method with the locking semantics. Injecting the lock would need to
be done in the wrapping method since `self` isn't defined when the decorator
is executed. Here's my first attempt:

```python
from threading import Lock

def synchronized(method):
    """
    A decorator object that can be used to declare that execution of a particular
    method should be done synchronous. This works by maintaining a lock object on
    the object instance, constructed for you if you don't have one already, and
    then acquires the lock before allowing the method to execute. This provides
    similar semantics to Java's synchronized keyword on methods.
    """
    def new_synchronized_method(self, *args, **kwargs):
        if not hasattr(self, "_auto_lock"):
            self._auto_lock = Lock()
        with self._auto_lock:
            return method(self, *args, **kwargs)
    return new_synchronized_method


class LinkedList:
    @synchronized
    def insert(self, data):
        pass
```

A nice first attempt but horribly broken - the `hasattr()` check on self and
the construction of the Lock object is not thread safe. Solving this problem is
simple enough if we can either ensure that `_auto_lock` is created in the
class `__init__` method or we introduce a new Lock somewhere else in the
process.

Solving the problem in `__init__` can take multiple forms. The simplest solution
is just require the user of the synchronized decorator to declare a member
called `_auto_lock` and raise an exception if the lock is missing. Then our
decorator would look like:

```python
from threading import Lock

def synchronized(method):
    """
    A decorator object that can be used to declare that execution of a particular
    method should be done synchronous. This works by maintaining a lock object on
    the object instance, constructed for you if you don't have one already, and
    then acquires the lock before allowing the method to execute. This provides
    similar semantics to Java's synchronized keyword on methods.
    """
    def new_synchronized_method(self, *args, **kwargs):
        if hasattr(self, "_auto_lock"):
            with self._auto_lock:
                return method(self, *args, **kwargs)
        else:
            raise AttributeError("Object is missing _auto_lock")
    return new_synchronized_method

class LinkedList:
    def __init__(self):
        # other init logic
        self._auto_lock = Lock()

    @synchronized
    def insert(self, data):
        pass
```

The downside here is that users are required to make two changes to their classes
in order to use the synchronized behavior. This also means that the developer has
read the documentation where we mention that they need to add `_auto_lock` to
the `self` instance. Relying on the user of our decorator is not as automatic
as the Java synchronized block.

The other solution is to introduce a Lock to prevent assigning `_auto_lock`
multiple times. The downside with this approach is the performance hit we need to
take when the synchronized method is called the first time on new object instances.
To try to make this process somewhat tolerable, we use the
[check-lock-check](http://en.wikipedia.org/wiki/Double-checked_locking)
pattern, where most long lived objects should skip the lock step for most cases. The code now
looks like:

```python
from threading import Lock

synchronized_lock = Lock()
def synchronized(method):
    """
    A decorator object that can be used to declare that execution of a particular
    method should be done synchronous. This works by maintaining a lock object on
    the object instance, constructed for you if you don't have one already, and
    then acquires the lock before allowing the method to execute. This provides
    similar semantics to Java's synchronized keyword on methods.
    """
    def new_synchronized_method(self, *args, **kwargs):
        if not hasattr(self, "_auto_lock"):
            with synchronized_lock:
                if not hasattr(self, "_auto_lock"):
                    self._auto_lock = Lock()
        with self._auto_lock:
            return method(self, *args, **kwargs)
    return new_synchronized_method

class LinkedList:
    @synchronized
    def insert(self, data):
        pass
```

We're now back to only a single change and we modify self in a thread safe fashion.
The downside is that we have this glaring performance bottleneck, the global instance
of synchronized_lock, across all sections of code that depend on synchronized. I'm
also leery of this solution because it does introduce risk of deadlocking in odd ways.

What we really need is more control over the class as the class is being defined and
hooks into the instance creation process.


### Metaclasses

Python provides the metaclass mechanics if you need more control over the creation of
classes or want to perform a bit more "magic" in your code. There are plenty of
resources that explain
[metaclasses in python](http://python-3-patterns-idioms-test.readthedocs.org/en/latest/Metaprogramming.html)
far better that I could and I would suggest taking a look at them before continuing.

The first order of business is to create `_auto_lock` automatically on self. The
only action that our user needs to take is to declare that their class definition uses
our metaclass. Here's our metaclass solution:

```python
from threading import Lock

def wrap_init_with_lock(orig_init):
    """
    A decorator function that will wrap a class __init__ method and adds
    an property called _auto_lock after running the original __init__
    method.
    """
    def new_wrapped_init(self, *args, **kwargs):
        orig_init(self, *args, **kwargs)
        self._auto_lock = Lock()
    return new_wrapped_init

class Synchronized(type):
    """
    This is our metaclass. It needs to be mixed in to objects that want
    to have the Java synchronized semantics.
    """
    def __init__(cls, names, bases, namespace):
        cls.__init__ = wrap_init_with_lock(cls.__init__)
```

Now let's use the new metaclass with the same LinkedList example:

```python
class LinkedList:
    __metaclass__ = Synchronized

    def insert(self, data):
        # ...
        pass
```

The first thing to notice is that the metaclass `__init__` method doesn't get passed
a reference to self (instance of `Synchronized`) but instead is passed an instance of
the object that is mixing in the behaviors of Synchronized (instance of `LinkedList`).
Also interesting is that since our code is being called as part of the object instance
creation process, we're in a thread-safe block of code.

Now we want to provide a synchronized version of our methods. Since we want to make usage
of the behavior as simple as possible, we need a new way to determine if a method should be synchronized.
We'll do this by naming convention - if the method name starts with `synchronized_` then
we'll wrap execution of the method to use `_auto_lock`. Using the `cls`
reference, we can use `dir` to find the methods with the matching names and then
[monkey patch](http://en.wikipedia.org/wiki/Monkey_patch) them with a decorator-like function.

```python
from threading import Lock

def wrap_init_with_lock(orig_init):
    """
    A decorator function that will wrap a class __init__ method and adds
    an property called _auto_lock after running the original __init__
    method.
    """
    def new_wrapped_init(self, *args, **kwargs):
        orig_init(self, *args, **kwargs)
        self._auto_lock = Lock()
    return new_wrapped_init

def wrap_method_with_sync(method):
    """
    A decorator function that wraps any class method with auto locking
    behavior. Before using, you need to ensure that the object reference
    has a _auto_lock property bound to an instance of threading.Lock.
    """
    def new_synchronized_method(self, *args, **kwargs):
        with self._auto_lock:
            return method(self, *args, **kwargs)
    return new_synchronized_method

class Synchronized(type):
    """
    This is our metaclass. It needs to be mixed in to objects that want
    to have the Java synchronized semantics.
    """
    def __init__(cls, names, bases, namespace):
        cls.__init__ = wrap_init_with_lock(cls.__init__)
        for methodname in dir(cls):
            if methodname.startswith("synchronized_"):
                orig_method = getattr(self, methodname)
                setattr(self, methodname, wrap_method_with_sync(orig_method))
```

With the LinkedList example again:

```python
class LinkedList:
    __metaclass__ = Synchronized

    def synchronized_insert(self, data):
        # ...
        pass
```

Now we have Java-like synchronized semantics based purely on the name of the method. It
also forces us to declare the synchronized functionality in the method name, serving as
good documentation for our code base. The metaclass is also a separate block of code that
can be unit tested and reused across the entire system.


### What's The Point?

The usefulness of these solutions is in the process of trying to bend and morph python
into a new and interesting shape through the various extension mechanisms that are provided
to us. Decorators are easier to understand but lack all of the extension points for a
problem as complex as our synchronized structure. Metaprogramming requires thinking about
the problem on a different plane, is difficult to wrap our mind around but also provides
us with a lot of ways we can change classes after the code has been written. As a kata,
the Java synchronized keyword has proven to be a nice sized problem.

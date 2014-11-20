---
layout: post
title: Java 'synchronized' in Python
tags: python kata
status: publish
type: post
published: true
comments: true
---
A recent student posed the question, \"Can we make using the Lock object in python
automatic on methods instead of needing to remember to write the code?\" My anwser
started with all the good reasons why we wouldn\'t want to hide the locking behavior
in magic, why adding it to all methods on all objects in python would be bad for
performance and testing and then to argue that the current syntax based on the with
keyword isn\'t that painful. Then I tried to see if I could replicate the problem
using some python magic.

<!--EndExcerpt-->

### Java-like synchronized Construct

After clarifying the requirements, we decided to replicate the synchronized keyword
in the Java language. If you aren\'t familar with Java, every object instance has a
mutex that can be locked by a single thread. The synchronized keyword when used in
a method definition locks on ```this```, the object instance. The end result is that
the developer can ensure a method is thead safe simply by stating:

{% prism java %}
public class LinkedList {
    public synchronized void insert(final Object data) {
        // ...
    }
}
{% endprism %}

To replicate this behavior in python, we\'d need two extra pieces of functionality.

1. Python objects do not implicitly contain a mutex lock, so we need to allocate
an instance of ```threading.Lock``` on self during object construction.

2. We need to acquire this implicit Lock when the method starts and then release
the Lock whenever flow exits our methods.


### Decorators

My first thought was to define a decorator, called ```synchronized``` that would
return a new method with the locking semantics. Injecting the lock would need to
be done in the wrapping method since ```self``` isn\'t defined when the decorator
is executed. Here\'s the first attempt:

{% prism python %}
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
    @sychronized
    def insert(self, data):
        pass
{% endprism %}

A nice first attempt but horribly broken - the ```hasattr()``` check on self and
the construction of the Lock object is not thread safe. Solving this problem is
simple enough if we can either ensure that ```_auto_lock``` is created in the
classes ```__init__``` method or we introduce a new Lock somewhere else in the
process. 

Solving the problem in ```__init__``` can take multiple forms. The simplest solution
is just require the user of the synchronized decorator to declare a member called
```_auto_lock``` and raise an exception if the lock is missing. Then our decorator
would look like:

{% prism python %}
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
{% endprism %}

The downside here is that users are required to make two changes to their objects
in order to use the synchronized behavior. This also means that the developer has
read the documentation where we mention that they need to add ```_auto_lock``` to
the ```self``` instance. Relying on the user of our decorator like this is highly
error prone. 

The other solution is to introduce a Lock to prevent assigning ```_auto_lock``` 
multiple times. The downside with this approach is the performance hit we need to
take when the synchronized method is called the first time on new object instances.
To try to make this process somewhat tolerable, we use the check-lock-check pattern,
where most long lived objects should skip the lock step for most cases. The code now
looks like:

{% prism python %}
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
{% endprism %}

We\'re now back to only a single change and we modify self in a thread safe fashion.
The downside is that we have this glaring performance bottleneck across all sections
of code that depend on synchronized. I\'m also leary of this solution because it does
introduce risk of deadlocking in odd ways.

What we really need is more control over the class as the class is being defined and 
hooks into the instance creation process. 


### Metaclasses



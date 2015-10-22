---
layout: post
title: The argument for s-expressions
tags: opinion PLT s-expressions
published: true
comments: true
teaser:
  Returning to Python after months in Clojure and I make a simple mistake that
  a s-expression language wouldn't have encountered. With a simple example, I'll
  show how syntax can cause an unnecessary cognitive load on developers.
---
I found myself back in our Python code base today working with a set of nested
dictionary objects. As I set out to look for the equivalent of Clojure's
```get-in``` function, I ended up at a [nifty solution using reduce][1]. Inspired,
I ended up with the following function in our Util module:

{% prism python %}
def get_in(d, keys, notfound=None):
    """Python implementation of Clojure's get-in function."""
    try:
        return reduce(dict.__getitem__, keys, d)
    except TypeError, exceptions.KeyError:
        return notfound
{% endprism %}

Of course this failed the unit test that I wrote:

{% prism python %}
class UtilTests(unittest.TestCase):
    def test_get_in(self):
        self.assertFalse(get_in({}, ["keya", "keyb"]), False)
{% endprism %}

The problem is with the except block of code in the ```get_in``` function - I
should have passed in the multiple types as a tuple instead of a comma separated
list. Implementing that small change causes the failing unit test to succeed.

It's pretty bad that python didn't complain as it was compiling the function
definition. What's worse is that I made the mistake and didn't have a clear idea
that it was a mistake (until I searched Google for try-except syntax). If python
used s-expressions, this type of silly mistake would not have come up.

{% prism scheme %}
(define (get-in d keys notfound=None)
        (try (reduce dict.__getitem__ keys d)
        (except '(TypeError exceptions.KeyError)
                notfound)))
{% endprism %}

Notice how it would be impossible to define a list of items to except without
representing them as a list. In fact, the only syntax mistake I can really make
with an s-expression language is forgetting to terminate an open expression with
a closing parentheses. I want to love the ML syntax of Haskell, PureScript, Elm
or Python but I'd rather use a handful of parentheses and spend the time thinking
about the problem I'm solving.

It's a good thing I decided to write a unit test before shipping. :)

[1]: http://stackoverflow.com/a/14484711/67927

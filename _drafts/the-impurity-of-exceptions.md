---
layout: post
title: The Impurity of Exceptions
tags: functional-programming patterns
published: true
comments: true
teaser:
  Tease
---
Chances are that the programming language you use for your every day job relies
on [exception handling][ex-handling] to deal with errors. On the surface,
exceptions seem to have some nice properties:

1. Since the exception is only exposed when caught, code only needs to worry
   about the error when it is clear that the error will be dealt with. This
   frees us from writing boiler plate code to propagate errors when a block of
   code does not know the best way to deal with the error.

2. Errors that are not handled anywhere in the application can be processed by
   the runtime environment. The most typical technique used by the runtime is
   to terminate the running application. This approach prevents the application
   from continuing in an unknown, incorrect state.


#### Trouble with Exceptions

The largest trouble with exceptions is that they introduce additional return
paths from your code. Let's take a simple example in Java:

{% prism java %}
public List<E> reverse(final List<E> inputList) {
  final List<E> newList = new ArrayList<E>(inputList.size());
  for(int i = inputList.size(); i >= 0; i--) {
    newList.add(inputList.get(i));
  }
  return newList;  
}
{% endprism %}

Seems pretty straight forward. We're allocating a new list set with a capacity
that matches the size of the inputList. We're then walking the inputList
backwards and adding those elements to our new list. Finally, we return the new
list to the caller. So how many return points are there?

Based the single return statement, most people would like to say that there is
one return point from the function - assuming that everything goes as you
intend. Others might read the java.util.* documentation and point out that
the add() and get() methods on List types can throw exceptions. We might also
be paranoid and include the ArrayList constructor as a possible place of
exceptions. Once we add in the possible NullPointerExceptions, we start to see
that our very simple method contains a lot more exit points than we thought of
when we wrote the code.

Let's ask another question - what exceptions should the caller of our reverse
method worry about? It's difficult to know, just based on the source code, what
exceptions may be thrown. Java attempted to solve this problem with a concept
called *typed exceptions*. Typed exceptions either had to be caught or the
method signature needed to specify the exception could be thrown from a method.
Let's take a look at an example using a common typed exception -
java.io.IOException.

{% prism java %}
public String readFile(final File txtFile) {
  final StringBuilder result = new StringBuilder();
  Reader reader;
  try {
    final char[] buffer = new char[2048];
    reader = new InputStreamReader(new FileInputStream(txtFile));
    while((int bytesRead = reader.read(buffer)) != -1) {
      result.append(buffer, 0, bytesRead);
    }
  }  
  catch(IOException ex) {
    final Logger log = Logger.getLogger("Example2");
    log.throwing("Example2", "readFile", ex);
    log.warning("readFile could not read file, returning default empty string");
    result.delete(0, result.length());
  }
  finally {
    try {
      reader.close();
    }
    catch(IOException ex) {
      // do nothing on purpose
    }
  }
  return result.toString();
}
{% endprism %}

This is a simple method that reads a text file and builds a single string out
of the content. If you try to omit catching the IOException, the compiler will
complain. Notice how I handle the exception - log a bunch of information and
then return a default value. Is that right? I don't know because I'm not sure
what context this method will be used. It could be ok if we're trying to read
a conf file that we have defaults for. If the user initiated this action through
a dialog, we might want to tell the user. If it's a dosage info file for a
medical device, we probably want to halt and do nothing.

With typed exceptions, we can fix that problem by adding the exception to the
method signature.

{% prism java %}
public String readFile(final File txtFile) throws IOException {
  final StringBuilder result = new StringBuilder();
  final char[] buffer = new char[2048];
  final Reader reader = new InputStreamReader(new FileInputStream(txtFile));
  while((int bytesRead = reader.read(buffer)) != -1) {
    result.append(buffer, 0, bytesRead);
  }
  reader.close();
  return result.toString();
}
{% endprism %}

Now it's very transparent to someone who wants to use my method that I'm doing
some kind of I/O and that they need to handle the failure. It also cleans up
the odd finally block handler in the first example. Why? Because the close()
method also throws an IOException.

> __Side Note__:
> The close() method throwing an IOException highlights the design problems
> with typed exceptions. Should close() notify the caller about the error or
> perform the best effort to close and call it a day. Most application
> developers don't care and that has lead to the introduction of the
> [quiet close pattern][close-quiet].

What if the calling code still doesn't have enough context? Well the options
are either add the typed exception to the method signature, do something bad
or *convert it to an untyped exception*.

{% prism java %}
public void loadConfFile() {
  try {
    final String content = readFile("/etc/myapp.conf");
    parse(content);
  }
  catch(IOException ex) {
    throw new RuntimeException(ex);
  }
}
{% endprism %}

Yes, Java let's you nest exceptions and by creating a new RuntimeException from
a typed exception, you can silence the compiler.

TODO: something about the typed vs untyped exceptions

TODO: incorporate C++'s handling

TODO: Transition


#### Coping with Exceptions

Exception handling isn't new, so how have we learned to cope with exceptions
in our code. For starters, we've learned to just accept software crashes, reboots
and overall flaky behavior as a society. There are [websites][daily-wtf] devoted
to all the stupid error messages that we encounter and deal with.

For long lived applications, like web servers or networked services, developers
often time resort to using the [Pokemon Exception][pokemon-exception] pattern
- find the highest level execution point for your application and:

{% prism java %}
public static void main(final String[] args) {
  try {
    //.....
  }
  catch(Throwable ex) {
    // gotta catch 'em all
    Logger.getLogger("application").log(Level.SEVRE, "unhandled exception", ex);
  }
}
{% endprism %}

In this example I did little more than print off a message to the log system
that may or may not been seen by someone. Worse yet is:

{% prism java %}
public static void main(final String[] args) {
  try {
    //.....
  }
  catch(Throwable ex) {
    // do nothing
  }
}
{% endprism %}

It might seem reasonable to squash the exception but then you're not getting
any information about what's happening in the code. I ran into this exact same
issue at BlackBerry (tm) years ago. The bug report had all sorts of wild guesses
about what kind of email was causing the bug. I eventually solved the mystery
when a noticed a class "do nothing" exception handler. Simply adding a log
statement showed that it wasn't a problem with the email. It was just a
NullPointerException in a block of code somewhere.

The [go language][go-lang] takes an interesting step back from exceptions and
resorts to returning errors as values from functions:

> "We believe that coupling exceptions to a control structure, as in the
> try-catch-finally idiom, results in convoluted code. It also tends to
> encourage programmers to label too many ordinary errors, such as failing to
> open a file, as exceptional.
>
> Go takes a different approach. For plain error handling, Go's multi-value
> returns make it easy to report an error without overloading the return value.
> A canonical error type, coupled with Go's other features, makes error handling
> pleasant but quite different from that in other languages.
>
> Go also has a couple of built-in functions to signal and recover from truly
> exceptional conditions. The recovery mechanism is executed only as part of a
> function's state being torn down after an error, which is sufficient to handle
> catastrophe but requires no extra control structures and, when used well, can
> result in clean error-handling code."
>
> -- FAQ: [Why does Go not have exceptions][go-no-exceptions]?

Instead go makes use of it's multiple return capabilities to lay forth a
convention that the error return value will always be after the functions
normal return value. combined with multiple forms in the if, you have a very
concise way to handle errors without exceptions:

{% prism go %}
f, err := os.Open("filename.ext")
if err != nil {
    log.Fatal(err)
}
{% endprism %}

Or

{% prism go %}
if err := dec.Decode(&val); err != nil {
  // ...
}
{% endprism %}

Minus a little bit of syntactical magic, the same pattern could become a
convention in our Java code base using the [Pair class][java-pair].

{% prism java %}
public Pair<InputStream, Throwable> Open(final String filename) {
  Pair<InputStream, Throwable> returnValue;  

  try {
    returnValue = Pair.with(new InputFileStream(new File(filename)), null);
  }
  catch(Throwable ex) {
    returnValue = Pair.with(null, ex);
  }

  return returnValue;
}
{% endprism %}

Structuring my code this way does avoid the problems associated with the
exception. Unfortunately, there is nothing that forces me to deal with the
error. The compiler forced us to deal with the exception or pass it on when
we had typed exceptions.

#### Solutions

In essence, coming up with a good error handling scheme is a tough task. As
we've seen, exceptions aren't much better than the sentinel return values from
C functions or error registers in assembly. In fact, they increase the
complexity of our code simply by obscuring the flow of the code and relying on
good developer documentation. Java tried to implement some compiler help with
typed exceptions but we find it easier to just fool the compiler by turning
typed exceptions into untyped exceptions.

In the next post, I'm going to explore if we can write some Clojure code such
that the compiler will force us to deal with the errors in a way that doesn't
require more work for ourselves.

[ex-handling]: https://en.wikipedia.org/wiki/Exception_handling
[close-quiet]: http://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/IOUtils.html#closeQuietly(java.io.Closeable...)
[daily-wtf]: http://thedailywtf.com/series/errord
[pokemon-exception]: http://c2.com/cgi/wiki?PokemonExceptionHandling
[go-lang]: https://golang.org/
[go-no-exceptions]: https://golang.org/doc/faq#exceptions
[java-pair]: http://www.javatuples.org/apidocs/org/javatuples/Pair.html

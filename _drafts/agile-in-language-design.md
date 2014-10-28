---
layout: post
title: Agile in Language Design
tags: agile
status: publish
type: post
published: true
comments: true
---
For months, I had a number of early morning discussions with [Kyle Rowland][kyle-rowland-blog] about the craftmanship of software 
development and the merits of functional programming vs object oriented programming. While we agreed to disagree on FP vs. OOP, we both
saw the need to release software faster and gather feedback. As we would list off good examples of agile software, we never once listed
a programming language. This thought started to surface again while revisiting the Python 2 vs. 3 conundrum and this time, I started to
wonder why we didn\'t see more languages releasing new features in smaller chunks and still maintaining the stability of their compilers
and runtime environments. The answer lies in macros.
<!--EndExcerpt-->

### Enter Macros

Language designers have the tool they need in order to make the development of languages more agile - [macros][macro-def]. The simplest
macros simply produce a new output structure that replaces the macro usage in the resulting source file. Some of the earliest forms of
macros appeared in C and were used to inline common code snippets.

{% prism c++ %}
#define MIN(X,Y) ((X) < (Y) ? (X) : (Y))

int a = 5;
int b = 12;
int c = MIN(a,b); //=> ((a) < (b) ? (a) : (b))
printf("value of c = %d", c); //=> value of c = 5
{% endprism %}

The macro system in C is a very primative - it simply subsitutes the definition pattern in ever location is sees the definition pattern.
In our example above, the compiler never knows that the value to assign to ```c``` is a macro. Instead the tetrary conditional operation
is substituted in its place and that is what the compile will evaluate. This simple macro language allowed the developers to encapsulate
boilerplate code in a reusable form and also inlined the definition throughout the code base. On slower platforms, the inline versions
would avoid the cost of a function call for a simple operation.


### The Problem With Early Macros

The C/C++ macro system was far from perfect and contained a number of hidden landmines for developers. Many of these early macro systems
relied on a pre-processor program to scan the source code prior to compliation and generate an intermediate format after evaluating the
macros. This leads to slower compile phases, as multiple scans of the source code is required, as well as bizzare compilation error messages
resulting from a misused macro. Both of these effectivly reduced the developers productivity sitting in front of the keyboard.

Slow compliation wasn\'t the only problem with early macro systems. Many of these early implementations were simply text substitution rules
that blidly emitted source code inside of the invocation context. As macros grew more complex, they often needed to create variables but what
should the variable name be? If the code is being blindly emitted into an arbitrary section prior to compliation, there is no good way to
know what\'s in scope. Along the same lines, the emitted macro result cannot be checked prior to compiling the context they emiited code into.
Sure, we could check that the emitted source was syntatctly correct but not much more. Solving both of these problems requires a macro syntax 
that\'s more complex than pattern matching and substitution.

As developers were starting to cope with these problems, the need to worry about manually tuning code to run performant was also 
becoming a moot point. Led by Sun Microsystems, the industry spent a number of years researching and then implementing new 
[JVM optimizations][jvm-perf-improvements]. The JVM was able to perform profiling as the application ran and reshape the running code
to offer the best performance. At the same time, hardware was pushing us towards the multi-core future where additional performance was
made simply by dividing problems into smaller, concurrent chunks. Combined with the [renewed interest in non-blocking I/O][c10k-problem], 
our applications achieved a tremendous performance gains without manual intervention. For the time being, it seemed that macros were no
longer needed.


### Hygenic Macros

Thankfully, the Scheme group continued to address the problems with early macro systems and eventually implemented a hygenic macro. Hygenic
macro compilers create seperate scopes (or environments) that the emitted code is executed in. The side effect is that new variables being
referenced in the macro now shaddow those defined in the context they were invoked in - solving the name collision problems. We also avoid
the name mangling hack that can lead to very fun debugging error messages at 2am on the weekend in production.


### Macros Are Agile

Once equiped with a rich, hygenic macro facility, extending a programming language is no longer an activity for the core language maintainers
nor does it require that every deployment opt in to some new feature.  This is the heart of the agile movement - release working features as
soon as possible. Additionally, the core language team can reduce their risks by postponing the incorporation of a macro-based library into
the core until it\'s deemed stable and ready for production by the community at large.

Recently this approach has been adopted by Clojure in the development of their async library, [core.async][clojure-async]. Unlike the go language,
core.async implemented all of it\'s functionality inside of functions and macros - including the [go][clojure-go-macro] construct, which is 
core to the whole async approach. Developers using a variety of Clojure versions were able to opt-in to this new async approach and start testing
it out without the need for a bump in the language version. Unfortuantly, the go developer is still going to have to wait for the core team to
priortize any new concurrency features and wait for a release.

[Elixir][elixir-homepage] takes the idea of using macros to build out the language even futher. In fact, most of the Elixir libraries and
functionality is written as macros. This gives the team rapid ability to prototype new ideas without the need to worry about breaking the
contract the language provides to software already written. (citations needed)


### Macros FTW

The next breed of languages need to support a more rapid development model - we simply don\'t want to wait for over a decade for new features
in our programming languages. Macros are a proven construct that enable language designers to iterate with new ideas without the need for
major changes to the underlying runtime or compiler. Using macros, we can opt in to testing out the new features in our languages that solve
our problems or wait for others to test out these new libraries. Languages that haven\'t supported macros are now starting to get the hint
and add them in. Languages that don\'t will end up in the \"legacy\" column.

In another post, I\'ll discuss how a well thought out macro system will a good type system can form the foundation for a powerful platform.


[kyle-rowland-blog]: http://software4profit.wordpress.com/
[macro-def]: https://en.wikipedia.org/wiki/Macro_(computer_science)
[jvm-perf-improvements]: https://en.wikipedia.org/wiki/Java_performance#Virtual_machine_optimization_techniques
[c10k-problem]: http://www.kegel.com/c10k.html
[go-lang]: http://golang.org/
[clojure-async]: https://github.com/clojure/core.async
[clojure-go-macro]: https://www.youtube.com/watch?v=R3PZMIwXN_g
[elixir-homepage]: http://elixir-lang.org/

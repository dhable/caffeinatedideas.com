---
layout: post
title: Agile in Language Design
tags: agile
comments: true
teaser:
    An interesting conversation about agile development practices one morning with Kyle Rowland 
    leads to some deep reflection on how language development can be more agile. Turns out that
    macros, as long as they're not in C, can make language design agile.
---
Every so often, you work with that engineer that challenges the way you think about a problem. For me, it was working with
[Kyle Rowland][kyle-rowland-blog]. Kyle had identified some serious antipatterns in the development process and was looking
for help in correcting the problem. This led to many conversations about software craftmanship and how the agile practice
were intended to work. While we disagreed on many things, we both agreed that software needs to be delivered faster and with
a feedback loop. As we discussed these points, I started to pay attention to a forgotten area of computer science that hasn\'t
acted very agile in the past - programming languages.

The idea of using agile in programming language development wasn\'t obivous at first. I had been exploring new languages that
have gotten a lot of attention for the past year. Almost all of the new languages contained a few traits in commmon - they were
dynamically typed, with optional type checking libraries and they all had some support for macros. One can argue that both features
make writing correct programs more difficult, it has the opposite effect on the programming langauge developers. No longer do
they need to worry about maintaining strong backwards compatability or touching internal parts of the compiler to add new features.
I\'ll  leave the static vs. dynamic argument for another post. Instead I want to focus on [macros][macro-def] and why embracing 
them doesn\'t imply your code will be unreadable.


### Enter Macros

In the early days of computing, the hardware ran slow. So slow that jumping from the current code flow to another function
took a measurable amount of overhead for frequently used instructions. To get around these problems, language designers came
up with a way to inline a function, or copy the implementaton to every used location. This would make the compiled code larger
but at runtime, the hardware wouldn\'t need to jump to a new location in memory for a simple funciton. The C language introduced
this optimization with macros.

The simplest macros from C performed nothing more than simple text replacement in the source file, very much like the early
web templating libraries.  Without access to the AST in the compiler or details of the surrounding context that the macro was
being evalulated in, the macro processor has to rely on the developer to know how the macro was going to expand. In this C example,
the ```MIN``` macro simply expands to a tetrary operator that the compiler can understand.

{% prism c++ %}
#define MIN(X,Y) ((X) < (Y) ? (X) : (Y))

int c = MIN(5, 12); //=> expands to 'int c = ((5) < (12) ? (5) : (12))'
printf("value of c = %d", c); //=> value of c = 5
{% endprism %}

The compiler never knows that the value to assign to ```c``` orginiated as a macro. Instead the tetrary conditional operation
is substituted in its place and that is what the compile will evaluate. Now if we misudnerstand the use of this macro and pass in
(and hopefully you wouldn\'t make this mistake) strings, the compiler would be given some nonsense.

{% prism c++ %}
int c = MIN("5", "12"); //=> expands to 'int c = (("5") < ("12") ? ("5") : ("12"))'
printf("value of c = %d", c); //=> value of c = 5
{% endprism %}

Hopefully our code now fails to compile because we can\'t cast a string to an int. The downside is that most C compilers will attempt
an implicit cast to make something meaningful out of the nonsense. This causes our code to compile and very strange runtime behaviors.
Even if the compiler caught the string to int cast, using floating point numbers will work and cause a loss in precision. We might catch
the problem if we looked through the warnings or ran the code through a lint tool. This hidden behavior and lack of compiler checking
caused a backlash from developers who got burned.

While macros had their problems, careful use enabled the earliest form of DSLs. This was a technique largely taken in the graphical
user interface libraries, most notiably Microsoft\'s MFC framework and the Qt framework. In each framework, you would sprinkle very
specific macros that looked like keywords throughout the code. These marcos then took on the overhead of building out patterns, such
as Qt\'s slots and signals event handling pattern. 

# Needs work from here on out #


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

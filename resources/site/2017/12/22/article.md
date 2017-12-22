I've been fortunate enough since April 2015 to work in a position where I was
expected to write and support Clojure code as one of my primary responsibilities.
Over that time, Clojure has changed me as a programmer. It wasn't until I listened
to [episode 68][ep68] of the [Functional Geekery podcast][podcast] that I really
reflected on how learning a lisp language has changed me.

Many people who work with a lisp language tend to speak of this transcending experience
and enlightenment that comes with learning a lisp. After hearing these kinds of vague 
experience reports and all sorts of success stories with Clojure, I was determined to
become a Clojure developer. I pushed Clojure at QThru, tried to introduce it at Medio 
and even selected Clojure as a key technology at jetway.io. Through all of this, I
couldn't tell you an exact reason other than elegance and beauty - neither of which solved
a business problem for me.

Even after getting on a team of existing Clojure developers, there wasn't a clear reason
why Clojure or lisp languages in general would transform my career and life. This cargo
cult of lisp being a transcending experience in some ways leads to intimidation and
elitism. To be honest, a lisp language has the same learning curve that any programming
language has. You'll spend the first block of time just kicking the tires and figuring
out how to write the most basic programs. Then you'll struggle with shifting your mind
to solving problems in a lisp idiomatic way. After clearing that hurdle will be the struggle
with making the code clean and understandable months and years later. Then comes debugging
complex issues in a new environment. None of this is made any easier because you use a lisp
language.

As [Matthew Butterick][mbutterick] mentions in the podcast, the interesting parts of list are the very
simple and homogeneous shape that all code takes. Unlike Java or Python, everything in a
lisp is a list of elements - where these elements may be functions to apply, arguments or
other nested lists. Once you understand that all artifacts of the code are the same, you
start to see that you don't need compiler support or external IDLs to express ideas. The
same building blocks of the language are available to you to extend the lisp into a new
problem domain. This is the essence of metaprogramming.

Many languages support metaprogramming. Python has metaclasses and Java has reflection.
Neither solution looks similar to writing a simple function that accepts and wraps other
functions. Neither can emit things at runtime without playing tricks with the code loader
or digging deep into the internals of the runtime environment. Code that leverages the 
[Javassist][javassist] library doesn't need to be anywhere as complex in a lisp dialect. 
Using a lisp lets you think about how you would solve the problem if you could change the
compiler or runtime. It's a very powerful ability if you're attempting to build a platform
or an extendable tool.

As I reflected on this, I can see how understanding macros and the power of metaprogramming
has given me a deeper meaning for what it means to express a solution to a problem. I don't
think it's something every developer needs to learn and experience either. But if you've
ever been frustrated with how to express a problem in your primary language, you might want
to learn a lisp and express how thoughts are expressed in code.


[ep68]: https://www.functionalgeekery.com/episode-68-matthew-butterick/#t=30:12
[podcast]:  https://.functionalgeekery.com
[mbutterick]: https://github.com/mbutterick
[javassist]: http://www.javassist.org
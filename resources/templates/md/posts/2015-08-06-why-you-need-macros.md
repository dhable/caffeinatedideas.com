{:title "Why You Need Macros"
 :layout :post
 :tags ["languages" "patterns"]}

In 2013, I started looking into a
[Elixir](http://elixir-lang.org/), a new language that
looks like Ruby and runs on
[BEAM](https://erlangcentral.org/tag/beam/). One of the more interesting
parts of the language is the early development of the
[macro](https://en.wikipedia.org/wiki/Macro_(computer_science) system,
an idea borrowed from Lisp. With macros, the Elixir developers were able to
iterate and provide a wide array of control structures without the need to bloat
the core language.

While many developers look at macros as things to avoid, they are essential in
building languages today. Without macros, a language needs to produce a big
design first in order to ensure that the new feature fits within the large
language ecosystem. C# and Java are prime examples of slowly evolving features
that need to be blessed before they can be used. This leads to long release
cycles where the cost of failure is high. Yet, the Clojure community was able to
introduce [core.async](https://github.com/clojure/core.async) without requiring any
impact to the core language library. At the heart of
[core.async](https://github.com/clojure/core.async) is a
[complex state machine](http://hueypetersen.com/posts/2013/08/02/the-state-machines-of-core-async/)
abstracted through a series of macros.

While the virtues of macros has been bouncing around in my head, I didn't have a
clear expression of the idea until I listed to
[Robby Findler's](http://www.eecs.northwestern.edu/~robby/talks/index.html)
Chicago 2015 Lambda Jam keynote on macros to prototype other languages. While the
actual keynote video hasn't been posted, Robby has given the same talk in other
forms and is well worth a listen.

<iframe width="560" height="315" src="https://www.youtube.com/embed/GBpfOpk-ZBU" frameborder="0" allowfullscreen></iframe>

As software continues to eat the world and we push the limits of complexity, we
need to apply agile approaches to the platforms we select, starting with the
languages we express our ideas in. Just as function, local scoping and module
system have become a necessity for languages in the past, macros will become a
must have going forward. Languages that don't have complete macro support will
fail to keep up and soon be forgotten.

Does this mean you should incorporate the use of macros into your application?
Sure. Pulling in libraries that abstract patterns, like HTTP ReST definitions,
should be readily used to reduce boilerplate code and implement best practices.
Master craftspeople with their platform will probably understand how to write
macros to implement loops and control structures but will write them sparingly.

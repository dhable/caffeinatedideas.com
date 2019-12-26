Over the last couple of months, I've worked on migrating my static site generator code from [boot][boot-clj] to
Clojure's new [deps][deps] tooling. [Deps][deps] is fairly minimal in what it provides and that turns out to be
exactly what I need.

## Background

Back in 2017, I outlined my motivation for [building my own static site generator][building-site-generator]. At
the time, I selected [boot][boot-clj] as the platform to build off of. [Boot][boot-clj] abstracted a lot of 
concepts common to build systems and had a large plugin ecosystem that I would leverage for various aspects - like
syncing files to AWS S3. This allowed me to focus my efforts on getting the representation of the site content
correct and building out the core functionality.

Unfortunately the maintenance of the [boot][boot-clj] solution was difficult. Clojure 1.9 introduced spec
along with a number of specs for the core library. A few published plugins and their dependencies triggered some 
spec failures. This is understandable with any new feature release but those plugins were never updated. In fact, 
it seemed that much of the [boot][boot-clj] ecosystem didn't have a lot of updates. After a year of waiting for
updates to conform to the specs, I had to face a tough choice - do the update myself or find a new plugin.

I didn't really want to fork the plugin for this fix. Forking then means maintaining the forked version. I suppose
I could have made a single fix and told anyone else who found the fork that they really should depend on the main
repo instead. I knew that the fork wasn't going to be my main focus and bringing more under supported code into
the world wasn't something I was interested in. I started looking on rewriting my code with a new plugin.

## Finding Deps

As I procrastinated on rewriting part of my application to work around an abandoned plugin, I ended up seeing
some chatter in the [Clojurains slack channel][clojurians] about the CLI and this thing called [deps][deps]. The 
idea of another dependency management tool didn't seem interesting. The part that really got my attention was the 
dismissal of the plugin architecture. Up to this point, [leiningen][lein] and [boot][boot-clj] had maintained this 
notion that you could extend the tooling but you needed to write a plugin to their specifications. I'm already crunched 
for time with some of my projects, I don't want to learn the APIs of build tooling nor do I want to struggle to fit my 
solution into some other framework. [Deps][deps] instead had you just write plain old Clojure programs instead. 

For a static site generator, this is ideal. Most of the niceities provided by build tooling isn't needed by my 
static site generator code. In fact, it just creates additional complexity that I would need to work around or
ignore in large parts of the code. It turns out that building the generate application was the same amount of
code with or without [boot][boot-clj]. What about file syncing with S3? Turns out that a sync program that
uses the [Cognitect AWS API][aws-api] isn't that difficult to write either. The upside now is that the code is all 
within my control and maintainable by me.

## Is it worth it?

At this point I wouldn't recommend throwing out [leiningen][lein] or [boot][boot-clj]. Building and packaging code
artifacts is a complex problem. The effort put into these tools to make that efficient or work through subtle bugs
with complex builds isn't going to be a simple application you want to create. Even the [test runner][test-runner] from 
Cognitect has some rough corners that you need to work through if you want to adopt it in your build. Hopefully this
becomes nicer as the community adopts the [deps][deps] tooling.

I do think you should consider migrating if your Clojure code gets deployed on services you host and you can afford
to run the application from source (i.e. no AOT compilation). The simplification of dependency management with a hook
to just start your app's main function is powerful and easy to understand/troubleshoot. 


[boot-clj]: https://boot-clj.com/
[deps]: https://clojure.org/reference/deps_and_cli
[building-site-generator]: https://caffeinatedideas.com/2017/08/25/building-my-own-static-site-generator.html
[lein]: https://leiningen.org/
[aws-api]: https://github.com/cognitect-labs/aws-api
[test-runner]: https://github.com/cognitect-labs/test-runner
[clojurians]: http://clojurians.net/
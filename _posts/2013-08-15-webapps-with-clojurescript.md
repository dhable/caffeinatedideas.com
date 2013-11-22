---
layout: post
title: Webapps with Clojurescript
tags: programming
status: publish
type: post
published: true
comments: true
---
In the late 90s and early 2000s, Java and C# sparked a fundamental change in
how software is developed. With the introduction of fast garbage collection and
VMs executing bytecode, developers stopped worrying about mundane issues and
were free to tackle much more complex problems. This has lead to large system
components like hadoop, cassandra and solr, which give the world a huge base to
build the future on. Yet, software development now has new problems in terms of
concurrency and correctness that Java and C# fail to provide ideal solutions to.

A few years ago, I started looking around at programming languages that I could
use for the next decade in building the future. One of the languages that I kept
revisiting has been clojure. Initially I was turned off by the LISP syntax but 
have since started to appreciate the minimal syntax rules and the elegance of 
immutable state. Add in the dynamic nature of the language and you have a very
compelling language for expressing what a web application involves.

<!--EndExcerpt-->

The only downside to Clojure for web applications becomes the JVM. Operationally,
the JVM is overkill for running simple web applications that focus more on reading
and writing data for the user. The JVM does even less when we start putting rendering
and formatting logic into the user's browsers via Javascript in most applications.
Fortunately, node.js provides a very light weight server side solution that is tuned
for modern web development paradigms. The setup is light weight and applications
execute quickly thanks to the V8 Javascript engine.

Luckily for us, Rich Hickey realized that Javascript and node.js are quickly becoming
the foundational fabric that modern web development will be built on. In 2011, the
clojure community started working on Clojurescript. Clojurescript is a dialect of
Clojure, with a handful of differences, that compiles into highly tuned Javascript
instead of JVM bytecode. Now you can express ideas in Clojurescript and run the
resulting code in any modern browser or hosted on the server side in node.js.

This allows developers to use the same language in both the browser and the server
component, such like node.js, with a programming language that contains a very
consistent and rich set of syntax rules. It also means that deployment of the full
application stack is cheap and easy thanks to the small node.js runtime and V8
engine. With automation, this process leads to a demand that can quickly ebb and
flow with application demand - the on demand computing future.

Getting started with a complete Clojurescript stack is fairly painless if you use
the Leinengen build tool. I won't cover how to setup Leinengen since it's already
throughly documented on its' Github page. With Leinengen installed, you'll need a
simple project file to start with.

{% prism clojure %}
(defproject web-ui "0.0.1-SNAPSHOT"
  :description "Simple webapp written in Clojurescript and hosted on node.js"  
  :plugins [[lein-resource "0.3.1"]
            [lein-cljsbuild "0.3.2"]]
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :source-paths ["comp/clojurescript/clj"
                 "comp/clojurescript/cljs"]
  :cljsbuild {
    :builds [{:source-paths ["src/client"]
              :compiler { :output-to "target/app/resources/js/client.js"
                          :optimizations :simple}}
             {:source-paths ["src/node"]
              :compiler { :output-to "target/app/server.js"
                          :optimizations :simple
                          :target :nodejs}}]}
  :resource {:resource-paths ["resources"]
             :target-path "target/app/resources"}
  :hooks [leiningen.cljsbuild leiningen.resource])
{% endprism %}

There looks like a lot here, but it's fairly straight forward. On lines 3 and 4,
the project file brings in a few Leinengen plug-ins - one for copying resource
files from a source location to the destination and cljsbuild to make it easier
to integrate Clojurescript into Leinengen.

__Quick Note:__ I ran into a bug with lein-resource 0.3.0 where the contents
of the resource file were replaced with the name of the file. Not super helpful
but I do know that 0.3.1 fixed this problem.

Lines 6 and 7 are used to declare where the Clojurescript sources are kept relative
to the project. You can omit these lines but I've found a few occurances where
I'll get a failure while generating Javascript from Clojurescript that was fixed on
the main line but the lein-cljsbuild plugin won't pick up the new version for some
time. By putting the source in your own project, you can always pull down or patch
the Clojurescript compiler as you need to.

Lines 8 through 15 are the heart of the Clojurescript project configuration. Here
we define two builds - one for the client side and one for the server side. When
you compile Clojurescript into Javascript, the native output is a single file that
contains all the cooresponding Javascript. Keeping the client side and server side
separate will make each file smaller and prevent clients from downloading tons of
code that won't ever be executed. There also currently seems to be a problem with
the node.js code generation and anything but simple optimizations. YMMV, but I've
found simple as a sufficent optimization level for myself at this point.

Lines 16 and 17 copy over all the static resources to the output bundle directory
so we can serve them up as the user browses our site. Finally, line 18 ties all
of the lein-cljsbuild tasks into the standard Leinengen build targets. This allows
you to specify lein build instead of lein cljsbuild compile.

With the project file defined, let's clone the Clojurescript repository into /comp.
Remember, you can skip this step if you remove the source-path decleration from the
project file if you're willing to live with the standard Clojurescript version used
by lein-cljsbuild. For now, it's just easier to manage the source yourself while
development on Clojurescript is fairly active.

This is the basis for our new Clojurescript webapp. In the next part, I'll review 
how to build out the backend server using the node.js framework restify.


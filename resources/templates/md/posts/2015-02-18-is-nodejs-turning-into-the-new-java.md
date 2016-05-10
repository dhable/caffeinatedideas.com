{:title "Is node.js turning into the new Java."
 :layout :post
 :tags ["java" "node.js"]}

It's been quite a ride for for node.js. A group of core developers secured a
[nice series A round to start npm, Inc.](http://venturebeat.com/2014/02/11/former-node-leader-takes-big-money-launches-node-startup/),
bringing a corporate steward to the growing repository site. Success stories on node.js in
[various](https://engineering.groupon.com/2013/node-js/geekon-i-tier/)
[companies](http://nodejs.org/video/) [seem](http://techblog.netflix.com/2014/08/scaling-ab-testing-on-netflixcom-with_18.html)
common place with new stories weekly. Recently,
[NodeSource closed their series A round](http://techcrunch.com/2015/02/09/nodesource-raises-3-million-to-build-new-programming-tools/)
to bring node.js to enterprise customers. All this excitement for node.js and JavaScript was succinctly summed up in 140 characters:

<blockquote class="twitter-tweet" data-cards="hidden" lang="en"><p>Brendan Eich: JavaScript&#39;s destiny is to fulfill Java&#39;s promise <a href="http://t.co/KFM59kIz20">http://t.co/KFM59kIz20</a></p>&mdash; Paul Krill (@pjkrill) <a href="https://twitter.com/pjkrill/status/566266357667221509">February 13, 2015</a></blockquote>
<script src="//platform.twitter.com/widgets.js" charset="utf-8"> </script>

The adoption of a new technology isn't the interesting part of the story. Engineers, hackers and technically minded
individuals like the challenge of learning and adopting a new technology. It's more interesting to see the blatant bashing
of Java, the technology node.js is trying to replace. For instance:

> Monolithic applications, mainly written in Java, are killing development cycles, stifling innovation and keeping
> Java-heavy IT organizations many steps behind their competitors, especially those who have embraced a microservices architecture"
>
> -- Joe McCann, NodeSource Co-Founder and CEO ([source](http://techcrunch.com/2015/02/09/nodesource-raises-3-million-to-build-new-programming-tools/))

This comment is only fair if you want to compare writing [Java 1.2](https://en.wikipedia.org/wiki/Java_version_history)
code with writing node.js v0.10 code.
The problem being highlighted isn't with Java, it's with the code being produced by the average Java developer.

The code being produced by the average Java developer sucks. It's not that Java hasn't kept up with advances in software
development. The problem is that the average Java developer is still using old patterns and libraries to solve
their problem. Take [non-blocking I/O (NIO)](https://en.wikipedia.org/wiki/Non-blocking_I/O_(Java)). Java added support for
NIO back in Java 1.4, but most answers on how
to perform I/O in Java highlight the java.io package. The same can be seen in how to build a service. We used to bundle things
in a WAR file and deploy to Tomcat. It was painful and lacked dependency isolation so people just built bigger services.
Today, the Java developer can use [Dropwizard](https://dropwizard.github.io/dropwizard/) to roll up your application into
a standalone service enabling true microservices.

Even though you can write Java that doesn't suck, developers won't use the new functionality in Java unless Oracle
breaks the language and improves the default behaviors. When node.js was created, the design of JavaScript made blocking I/O
impossible and thus callbacks were built into the platform.  Even though
[promises](http://blogs.msdn.com/b/ie/archive/2011/09/11/asynchronous-programming-in-javascript-with-promises.aspx) provide
a lot of benefits,
the average node.js developer will solve the problem with callbacks. Why?  The core library uses callbacks
so the concept is introduced early on. Armed with a tool, the average developer will continue to use that tool regardless
of better or more efficient tool that may be offered to them.

This pattern isn't new either - look at C++. Everyone who learned C++ was first introduced to allocation via pointers
and the ```new``` keyword. While better options existed, such as
[object allocation on the stack](https://en.wikipedia.org/wiki/Resource_Acquisition_Is_Initialization) or the use
of ```auto_ptr``` from the STL library, developers still continued to allocate memory with pointers. Combined with sloppy
designs, this led to [memory leaks](https://en.wikipedia.org/wiki/Memory_leak) and
[null pointer reference](http://stackoverflow.com/a/2727872/67927) bugs being common in
C++ code. Java solved this problem by making garbage collection the default regardless of how the object was allocated. In
the end, Java and its garbage collector won partially because it was the default behavior in the platform. (Funny enough,
Java and JavaScript still have memory leaks and null pointer reference bugs.)

All that aside, I love writing node.js code and if I'm honest with myself, it's not for any technical merits that I couldn't
accomplish in Java. It's simply because everything with node.js is a
[greenfield project](https://en.wikipedia.org/wiki/Greenfield_project) and there isn't
a lot of legacy code that sucks to deal with. It's the same feeling I had about Java in 2000. Yet if history is any indication,
the next few years will attract the average Java developer to node.js and with it the problems of sucky code that never gets
refactored.

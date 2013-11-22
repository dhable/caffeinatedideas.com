---
layout: post
title: Documenting ReST - Part 2
tags: development, languages
status: publish
type: post
published: true
comments: true
---
In part 1 of this post, I laid out the problem facing us and how I was going 
to tackle documenting our ReST API. After learning more about the JavaDoc tool, 
extending it seemed like the logical solution that didn't reinvent the wheel. 
To make this whole project more interesting, I also decided that I'd try my hand 
with Clojure. It's a language I've been meaning to play with for awhile and this 
seemed like a small enough task with some interesting meaty bits, such as exposing 
Java classes to other tools.

<!--EndExcerpt-->

The first task was to build the entry point from the JavaDoc tool. To do this, I 
simply need to create a new class that extends the com.sun.javadoc.Doclet class. 
This was my first challenge with Clojure. How was I going to create a class in 
Clojure that extended from a Java class? Turns out that Clojure contains a function 
called gen-class that would enable me to define exactly that.

{% gist 6005120 %}

The parameters to gen-class define all the metadata about the construction of your 
class. Most of the attributes are fairly obvious with a few points:

* Specifying a prefix is just a name mangling trick used when resolving the which Clojure 
  function handles a call to a given method. You'll need this if a source file defines a 
  number of classes that all have start() methods so they can all be mapped back to the 
  correct class.

* The methods attribute takes a list of methods and the metadata. The #^{} map proceeding 
  the method definition list is Clojure's metadata technique and is where we need to define 
  a static method (told you this had meaty bits). The method definition then includes the 
  name of the method, a list of parameter types and a return type. While Clojure might not 
  need types, Java does.

After stubbing out the process-doc-tree function, the next challenge was how to compile down 
into something Javadoc could run. Also, the down side to using gen-class is that the version 
of Clojure used to run the project also needs to be the same version that compiled the project. 
Luckly, the fine devs on the Leinengen project have you covered and included an uberjar task 
that makes a single JAR file with all resulting bytecode, the Clojure run time and any 
dependencies that you might use. With the uberjar in hand, I could then patch the JAR file 
into maven using:

{% prism xml %}
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-javadoc-plugin</artifactId>
  <version>2.8.1</version>
  <configuration>
    <doclet>restdoc.core.RestDoclet</doclet>
    <docletArtifact>
      <groupId>qthru</groupId>
      <artifactId>restdoc</artifactId>
      <version>1.0.0</version>
    </docletArtifact>
    <useStandardDocletOptions>false</useStandardDocletOptions>
  </configuration>
</plugin>
{% endprism %}

Success! After JavaDoc reads all the source code, my process-doc-tree method is handed an instance 
or com.sun.javadoc.RootDoc. Processing the document is now a nice recursive problem that is well 
suited for Clojure. Let's start with a function that outputs our markdown format for this 
RootDoc node.

{% gist 6005125 %}

A full document is composed of the document header, when the document was generated and 
the contents. After breaking down the problem into smaller chunks we can turn our attention 
to what the contents should look like. Again, we can build a function that detect when a 
method is annotated with the correct @restdoc tags and generates our output.

{% gist 6005129 %}

The nice part of using Clojure was that the problem continues to break down from a 
very large task, generating the entire document, into smaller and more manageable pieces 
that didn't require a ton of boiler plate code. If I was doing this in Java, I would 
have created over a dozen classes by now and had methods all over the place. Not to say 
that it was a perfect experience either. Trying to work with Java arrays using Clojure 
functions every now and then did things I didn't expect.

This was a nice project to dive into a new language with. It involved dealing with all 
the interop issues that a large system would need to deal with and had enough depth to 
need to dive into the Clojure library more than a guided tour would. I can't wait to use 
Clojure again.

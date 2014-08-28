---
layout: post
title: Documenting ReST
tags: clojure
status: publish
type: post
published: true
comments: true
---
As a startup, we only have a limited number of resources and a huge product 
backlog to get through (who doesn\'t). To accelerate delivery of a bunch of 
features, we had to de-agile our team and outsource parts of the development 
efforts. While we decided to outsource important items, we don\'t necessarily want 
to loose control of the cloud platform. This has lead to a silo between our backend 
service developers and the new outsourced group and some interesting communication 
concerns. The largest is, how do we communicate our API to those using the API?

<!--EndExcerpt-->

When I started researching how people document their ReST APIs, I found a lot of 
posts simply playing the \"it should be self documenting\" card. As a developer for 
the last 12 years, I knew that was the same misguided line that people have been 
saying in this industry forever - think [XML][xml-myths]. Frankly, there\'s a lot of 
information to convey and sometimes I need more than a verb and a URL.

The first thought was to look for a way to to include some extra information in the 
JavaDoc comments for the methods of my business objects that handle these API entries 
into our code. That way I wouldn\'t have to maintain a separate API document and the 
code comments that already contain a bulk of this information. The ideal comment would 
look something like:

{% prism java %}
/**
 * Retrieves a user's profile information from the server. This operation resets 
 * the inactive user timeout.
 *
 * @returns Output stream with the REST contents
 * @param dbSession An active handle to the database.
 * @param apiVersion The version of the API the user requested.
 * @param user The User object.
 *
 * @restdoc.endpoint /users/{email}
 * @restdoc.endpoint.method GET
 * @restdoc.endpoint.param {email} The email the user signed up with.
 */
{% endprism %}

Here you can see there are a number of things going on. First, there is extra information 
that the code knows that a ReST user doesn\'t need to know, such as database session 
information or other various parameters that aren\'t necessarily part of the API call. We 
would like to suppress that information. We also have some additional information, like 
endpoint and method, that are important to ReST but not necessarily exposed in the code 
(could be hidden in the framework). I didn\'t want to write another version of JavaDoc but 
I wanted to control how the documentation is generated.

As it turns out, the JavaDoc tool does allow for an extension via the [Doclet API][doclet-spec]. 
Invoking the JavaDoc tool with the -doclet command line switch will tell JavaDoc to parse 
all the doc comment blocks from the Java code, construct a comment document object model and 
then pass control to the custom Doclet with the comment DOM. The custom Doclet is then free 
to query the DOM for information about the comments and the artifacts each comment appear 
next to in the source code. 

I eventually was able to create a custom Doclet that generated a [markdown text][markdown-spec] 
document with all the information I needed from the code. Honestly, why not hand the documentation 
to another developer as a text file, the most universal file type developers know. 

In Part 2, I\'ll dig into constructing the Doclet plugin and the twist I added to my simple project.

[xml-myths]: http://workflow.healthbase.info/monographs/XML_myths_Browne.pdf
[doclet-spec]: http://docs.oracle.com/javase/1.5.0/docs/guide/javadoc/doclet/spec/index.html
[markdown-spec]: http://daringfireball.net/projects/markdown/

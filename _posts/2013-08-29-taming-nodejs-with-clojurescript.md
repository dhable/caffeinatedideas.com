---
layout: post
title: Taming node.js with Clojurescript
tags: programming
status: publish
type: post
published: true
comments: true
---
In my [previous post][prev-post], I outlined a new, lighter weight approach 
to building web applications using Clojurescript and node.js as the 
runtime environment for the backend. The nice part of Clojurescript is that 
it doesn't try to abstract away the host environment so all those useful 
node.js packages can be reused without the need to wait for someone to rewrite 
them in Clojurescript. 

<!--EndExcerpt-->

To start, we need to pick a framework for our web development. My personal 
preference is [restify][restify-web] since it comes out of the box with support 
for API versioning, a static file server and handles a fair number of ReST conventions
without any configuration. The only thing that it lacks is built in support for
a page templating library, but this isn't important for our application. All of
the pages in our app will be static HTML files and we'll use client side 
Clojurescript to load data and respond to the user actions.

Now that we have a runtime selected, we can generate a very simple server that
contains an API for greeting the world. Place the following server.cljs file in
the src/node/example directory.

{% prism clojure linenos %}
(ns example.server
  (:require [cljs.core :as cljs]
            [cljs.nodejs :as node]))

(def times-greeted (atom {}))

(defn say-hello [request response next]
  (let [name request/params/name
        old-count (@times-greeted name)
        new-count (inc (if (nil? old-count) 0 old-count))
        response-body (cljs/clj->js {:name name :visit_count new-count})]
    (do
      (swap! times-greeted assoc name new-count)
      (.send response response-body)
      (next))))

(defn create-server []
  (let [restify (node/require "restify")
        server (.createServer restify)
        static-file-regexp (js/RegExp. "^/\\/?.*")
        static-server-opts (cljs/clj->js {:directory "./resources" :default "index.html"})
        static-file-server (.serveStatic restify static-server-opts)]
    (do
      (.get server "/greeting/:name" say-hello)
      (.get server static-file-regexp static-file-server))
    server))

(defn main [& args]
  (let [web-server (create-server)]
    (.listen web-server 3000)))
(set! *main-cli-fn* main)
{% endprism %}

This small block of code isn't that many lines but performs a fair about of work
in that limited space. The first three lines setup a namespace for our code to
live in. They also import two very important packages from the Clojurescript - 
the core package and the node.js specific package. We'll use those later on in
the code.

On line 5, we define an atom that contains a dictionary of users and the number
of times they've been greeted before. By design, the Clojure language treats all
variables as immutable unless we wrap the variable in an [atom][atoms]. If we 
wouldn't have wrapped the dictionary in an atom, the application would never 
change any of the values and appear broken.

On line 7, we define a function that can act as a restify request handler. This
means that it needs to accept a request object, a response object and a callback
function that indicates this handler is finished executing. Line 14 is where the
response is generated and sent back to the user by calling the send() method on
the response object.

Line 17 is the function that actually creates an instance of the restify server and
binds all the paths to their handlers. All of the global functions and variables
in node.js are part of the cljs.nodejs module, which we imported at the top. After
bringing in the restify module, you use the same function names that you would if
it was Javascript. In my case, I've setup the path /greeting/:name to bind to the
say-hello function. I also bind a static file server as the last handler to match
any path that hasn't been matched. This is how I'm able to handle requests for the
HTML pages with a single line of code.

Finally, a main function is defined to create a restify server and start listening
to port 3000 followed by a special Clojurescript directive that tells the compiler
that the function named main is truly the main entry point. This does differ from
node.js, where code at the global method of the first script is executed just like
it's a main function. Clojurescript, unlike plain JavaScript, is keeping the house
in order and places all the code in a namespace into a Google Closure namespace.
Without telling Clojurescript where to execute on startup, the node.js application
will create all the functions but never execute any of them.

Typing lein compile from the project root will generate a directory target/app that
contains all of the resources from our project and a file called server.js. This is
the Javascript that was generated from all the Clojurescript source files. It can
be read by a human, but I wouldn't recommend it. 

Running the compiled output still requires some manual steps. Recall that we wrote 
our application against restify but didn't download anything. If you were writing
an app in straight node.js with Javascript, you would have created a NPM project
file and used npm install to pull in this third party library. Well, we can do the
same thing here. Add the following package.json file to target/app.

{% prism javascript %}
{
 "name": "example",
 "private": true,
 "engine": {
   "node": "~0.10"
 },
 "dependencies": {
   "restify": ">=2.3.x"
 },
 "scripts": {
   "start": "node server.js"
 }
}
{% endprism %}

Now run npm install and then npm start to launch the server. The manual management
of this file is a bit painful. Ideally, we would like to describe the node.js
dependencies alongside our Leinengen project file and have a plugin generate the
package.json definition for us. A bonus effort would also let us execute start and
stop targets all from Leinengen. I'm thinking that this sounds like a good rainy
day project whenever I have the time.

As a comparison to Clojurescript, I've also provided the same application 
written in JavaScript to compare. In both cases, I could have used more terse
syntax to shorten up the code but opted to leave them expanded so it's easier to
compare the two.

{% prism javascript %}
var restify = require("restify");

var times_greeted = [];

var say_hello = function(request, response, next) {  
  var name = request.params["name"];
  var old_count = times_greeted[name];
  var new_count = (old_count ? old_count : 0) + 1;

  times_greeted[name] = new_count;
  response.send({name: name, visit_count: new_count});
  return next();
}

var create_server = function() {
  var server = restify.createServer();
  var static_file_regexp = new RegExp("^/\\/?.*");
  var static_server_opts = {directory: "./resources", default: "index.html"};
  var static_file_server = restify.serveStatic(static_server_opts);

  server.get("/greeting/:name", say_hello);
  server.get(static_file_regexp, static_file_server);

  return server;
}

var web_server = create_server();
web_server.listen(3000);
{% endprism %}

As you can see, they're not that different from each other. This begs the question, 
did we really gain anything by switching from JavaScript to Clojurescript? For starters,
Clojurescript ensures that the core functions and modules [behave with the semantics][js-wat] 
that we expect. Sure, we could leverage thrird party libraries to help in this regard but 
with the language enforcing the constrains, we know that the code base will have a consistent 
approach.

The more exciting gains from Clojurescript come in the form of [core.async][core-async-web] 
and [core.typed][core-typed-web] modules from the Clojure community. Without the need to change 
the runtime environment or the compiler, the community is bring asynchronous, type safe 
development to node.js. It's very compelling to be freed from nested callbacks and also have 
the option to enforce some type correctness in my code. The best part is that it's not a hard 
requirement so I can still prototype without defining types until I have a better understanding 
of the problem domain.

[prev-post]: {% post_url 2013-08-15-webapps-with-clojurescript %}
[restify-web]: http://mcavage.me/node-restify/
[atoms]: http://clojure.org/atoms
[js-wat]: https://www.destroyallsoftware.com/talks/wat
[core-async-web]: http://www.infoq.com/news/2013/07/core-async
[core-typed-web]: https://github.com/clojure/core.typed
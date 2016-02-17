{:title "JavaScript Dependency Injection Pattern"
 :layout :post
 :tags ["javascript" "patterns"]}

Over the course of the last month, I've been working on rebuilding a JavaScript SDK
for my employer. Once of the requirements was that all of the code needs to ship with
an automated test suite that capable of testing a majority of the basic functionality.
Throughout the process, I've relied a lot on dependency injection in order to provide
hooks to mock or stub out functionality in a fairly straight forward manner.

## Dependency Injection

In a nutshell, [dependency injection](http://en.wikipedia.org/wiki/Dependency_injection)
favors accepting external classes and module references as parameters. In code:

```javascript
// This block pulls in its dependencies
var Set = function() {
  var data = [];
  return {
    contains: function(element) {
      for(var i = 0; i < data.length; i++) {
        if(data[i] == element)
          return true;
      }
      return false;
    },
    add: function(element) {
      if(!this.contains(element)) {
        data.push(element);
      }
    }
  };
};

// This block's dependencies are pushed in
var Set2 = function(data) {
  return {
    contains: function(element) {
      for(var i = 0; i < data.length; i++) {
        if(data[i] == element)
          return true;
      }
      return false;
    },
    add: function(element) {
      if(!this.contains(element)) {
        data.push(element);
      }
    }
  };
};
```

Here we have two different classes that will form the basis for Set data structures that
back their data with an array. The first object, Set, is probably code that you've written
plenty of times before. The Set will eventually need to store the data and thus generates
an array as part of the constructor. The second object, Set2, will also eventually need to
store the data but instead of allocating the array, the constructor requires that the array
would be provided.


## Why Write DI Code?

Both sets perform the same basic functions, add and contains, and both have the same runtime
behaviors. So why should you prefer to write your objects using the second version? There are
really two major advantages to using dependency injection in JavaScript:

  1. Without a ton of work, I can now write a unit test that also has access to the
     injected property. This means that after running add() or before running contains(),
     I can manipulate the data to setup pre-conditions. I can also inspect the post conditions
     of the code because I can see what happened to the data structure after the test is done.

  2. JavaScript is dynamically typed so nothing says that data has to be of type Array. I
     could inject any object that implements the same fit, form and function of array and
     now my SetDI class can use a new backing data structure. This doesn't work so well for
     the array but think about all the DOM objects that could be replaced with other
     parts in various browsers without having to change your algorithm.

For me, the ability to test the code easily without the need to fiddle with 10th level JS
ninja tricks is huge. In a dynamic environment, all those tests that run against every change
will prevent you from shipping stupid mistakes and wasting time hunting down production issues.


## Dependency Injection Pattern

The code above showcases the basic idea behind dependency injection in plain JavaScript. Now let's
apply the to node.js and [browserify](http://browserify.org/). Instead of using the require function at the
top of the module to bring in libraries, let's export a single function that binds the dependencies
into the current scope.

```javascript
module.exports = function(_, fs) {
  // Private function definitions

  return {
    // Exported function definitions
  };
};
```

Here, the module looks like it takes [underscore.js](http://underscorejs.org/) and the node.js file system
modules as dependencies. With the dependencies captured in a closure, you can now implement
everything that would depend on underscore and fs. Remember, returning a named function in the
object literal is the same as exporting it from the current scope. Private functions can still
access the injected dependencies but need to be declared within the outer function but before
the return statement.

Using the code simply requires an additional function invocation.

```javascript
var _ = require("underscore"),
    fs = require("fs"),
    myModule = require("./myModule.js")(_, fs);
```


## This Looks Familiar...

Yup, it should if you've done any amount of work with [require.js](http://requirejs.org/). In fact,
[require.js](http://requirejs.org/) goes one step further and performs the injection step for you.
Unfortunately, [require.js](http://requirejs.org/)  isn't available in every JavaScript environment.
In those cases, you can use the above convention to still leverage dependency injection and
write testable code.

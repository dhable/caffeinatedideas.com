---
layout: post
title: Breaking Statically Typed Languages
tags: programming rants
status: publish
type: post
published: true
comments: true
---
Every time I tell my fellow Java developers that I enjoy writing code in python or node.js or clojure, 
I\'m very often dismissed with a set of reasons but the one I always enjoy is: 

> Java is statically typed so the compiler checks to make sure I don\'t make mistakes.
    
Yet many Java developers don\'t use the type system.

Well, they really do use the type system but just as a way of declaring that everything in the system is 
some built in type - typically integers, strings or booleans. 

Let\'s look at a method similar to something we\'ve probably all run across

{% prism java linenos %}
public User(String org, String email, String phone, String status) {
   this.org = org;
   this.email = email;
   this.phone = phone;
   this.status = status;
}
{% endprism %}

Seems reasonable, right? I think we\'ve all done this because it\'s fast, it doesn\'t require us to pause 
and build a huge class hierarchy, it\'s flexable to change over time, etc. Looks a bit like the python version:

{% prism python linenos %}
def __init__(self, org, email, phone, status):
   self.org = org
   self.email = email
   self.phone = phone
   self.status = status
{% endprism %}

In fact, the Java compiler can\'t help ensure that your code is correct. The string \"bob@caffeinatedideas.com\" 
is as valid as \"@bob\" to the compiler. Even if you did add that validation in the User object constructor, 
you\'d have to replicate that everywhere you accept or pass around an email address. There\'s no more guarantee 
that the value passed through one of those verified path. This is one of the pitfalls with dynamic typing - you 
need to write tests around all the permutations of the values every time you accept a value as an input.

Now we could build a strongly typed EmailAddr object and that\'s probbaly the right way to tackle this problem. 
With common type, like email address, we could use an open source solution. Sadly, most developers won\'t because 
the object either has odd dependencies, isn\'t straight forward to use or is lacking some major functionality 
that can\'t be mixed in. Look at all the Java libraries that use a string for a URL when java.net.URL has been part 
of the platform forever.

I\'m not advocating that we should drop everything and pick up tools with static typing. There are points that 
dynamic typing is a fantasic option and there are points that static typing is great. Just please stop using type 
checking and compilers as reasons your language is better when most of the code you write doesn\'t leverage them. 

---
layout: post
title: Scala Syntactical Heartburn
tags: scala rants
status: publish
type: post
published: true
comments: true
---
This morning I was working through the Scala chapter from \"Seven Languages in 
Seven Weeks\" by Bruce Tate and discovered there are three ways to apply a cumulative 
operation across all items in a list using foldLeft().

We start with the syntactical sugar version first:

{% prism java %}
val list = List(1, 2, 3)
val sum = (0 /: list) { (sum, i) => sum + i }
{% endprism %}

The /: operator is just a shorthand notation for the function foldLeft and could be 
written out in long form as

{% prism java %}
val list = List(1, 2, 3)
val sum = list.foldLeft(0)((sum, i) => sum + i)
{% endprism %}

The first approach, using the /: operator, doesn\'t make sense. I\'m not sure how I get 
foldLeft, inject, or whatever else you want to use to describe the operation out of this 
symbol. It reminds me a lot of the early C++ days when people would overload operators 
because it was cool and cut down on typing but took extra brain cells to remember what 
all the operators were overloaded to.

The second approach avoid the problems with non-obivious operators but just doesn\'t 
have the right flow for me. The second statement starts with list.foldLeft. This 
starts my mind thinking about C syntax with braces and dots but then finishes with a 
very parenthesis heavy block, which reminds me of list. This mixture of the two styles 
just doesn\'t feel clean to me.

Playing around on my own, I fixed my two objections by combining parts from the two

{% prism java %}
val list = List(1, 2, 3)
val sum = list.foldLeft(0) { (sum, i) => sum + i }
{% endprism %}

As you can see the block does\'t necessarily need to be passed in with parentheses but 
can appear like a typical curly braced block. To me, this fits the goal of being 
descriptive enough to read months down the road and maintains a consistent style. The 
fact that code can be very terse and symbolic and/or follow an inconsistent style is 
really why I\'ve had a hard time falling in love with Scala. I just feel like Scala is 
trying to be all things to all people similar to how C++ turned out. While I don\'t deny 
the power it brings to the table, we should be focusing on getting better instead of 
trying to find multiple ways of doing things.

For now, I\'ll keep at it and see if I feel different later down the road.

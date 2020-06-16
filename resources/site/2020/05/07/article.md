Back in 2012 I wrote a blog entry, [Scala Syntactical Heartburn][scala-syntax], in which I
said

> The first approach, using the /: operator, doesn't make sense. I'm not sure how I get foldLeft, 
> inject, or whatever else you want to use to describe the operation out of this symbol.

Well, that changed this week while reading [Functional Programming in Scala][red-book]. In chapter
3, there is a discussion on how to create an immutable, functional List type and doing interesting
operations on the list, like summing the list. Enter foldLeft and foldRight. 

Both foldLeft and foldRight work by transforming the multiple values in the list into a single
value of the same type. The difference between them is based on where application of the user 
supplied value happens - either to the head or the tail of the list.

```scala
List(1, 2, 3).foldLeft(0)(_ + _)  // is like (((0 + 1) + 2) + 3)

List(1, 2, 3).foldRight(0)(_ + _) // is like (1 + (2 + (3 + 0)))
```

Recall that functional programming and lambda calculus use the substitution model for computation.
The actual addition operation isn't applied until the fold operations have been fully substituted.
To make the next part easier to generally reason about, let's forgo infix operators in our
expanded versions and sneak lisp into a scala post :)

```scala
List(1, 2, 3).foldLeft(0)(_ + _)  // is like (+ (+ (+ 0 1) 2) 3)

List(1, 2, 3).foldRight(0)(_ + _) // is like (+ 1 (+ 2 (+ 3 0)))
```

Now we can graph each of the function applications where the function, + in our example,
is a node with two children pointers and the elements of the list, ints in our example, are
leafs.The tree for the foldLeft operations visually then becomes

```
       +
      / \
     +   3
    / \
   +   2
  / \
 0   1
```

and the foldRight visually becomes

```
   +
  / \
 1   +
    / \
   2   +
      / \
     3   0
```

When visualized this way, the symbolic versions of foldLeft, `/:`, and foldRight, `\:`, are
visual hints as to the order of function application. 

![Mind Blown](https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif)

[scala-syntax]: /2012/07/12/scala-syntactical-heartburn.html
[red-book]: https://www.manning.com/books/functional-programming-in-scala

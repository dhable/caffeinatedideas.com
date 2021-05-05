One of the big patterns in software is the idea of evaluating some piece of logic once. Normally 
this is done to obtain a value from a function or method call that takes some time to compute and 
Scala proposes using [lazy val][lazy-val] for this purpose.

```scala
@ def squareSlowly(i: Int): Int = {
   Thread.sleep(10000)
   i * i
}
defined function squareSlowly

@ squareSlowly(10)
// ... for 10 seconds ...
res1: Int = 100

@ lazy val tenSq = squareSlowly(10)
tenSq: Int = <lazy> // notice the immediate return

@ tenSq
// ... for some time up to 10 seconds ...
res3: Int = 100

@ tenSq
res4: Int = 100 // notice the immediate return
```

The problem here is that `tenSq` is a variable and not a function call. If you wanted to pass 
`tenSq` into methods or functions, you would need to use the [call by name][by-name] argument:

```scala
@ def useLazyInt(lazyInt: => Int): String = lazyInt.toString
define function useLazyInt

@ lazy val twentySq = squareSlowly(20)
twentySq: Int = <lazy>

@ useLazyInt(twentySq)
// ... delay of 10 seconds ...
res5: String = "400"

@ useLazyInt(twentySq)
// immediate return
res6: String = "400"

@ useLazyInt(5)
// immediate return
res7: String = "5"
```

Notice that `useLazyInt` doesn’t actually enforce the laziness of its argument, as shown in 
the last statement. What if we wanted the function that consumes the value to ensure 
[memoization][memoization] thus having the compiler enforce the behavior? [Eval][eval] does 
just that for us.

```scala
@ def squareSlowly(i: Int): Eval[Int] = Eval.later {
     Thread.sleep(10000)
     i * i
  }
defined function squareSlowly

@ def useSqEval(e: Eval[Int]): String = e.value.toString
defined function useSqEval

@ val fiveSq = squareSlowly(5)
fiveSq: Eval[Int] = cats.Later@5fdcc63f

@ useSqEval(fiveSq)
// ... 10 second delay ...
res12: String = "25"

@ useSqEval(fiveSq)
// ... no delay ...
res13: String = "25"

@ useSqEval(5)
cmd14.sc:1: type mismatch;
 found   : Int(5)
 required: cats.Eval[Int]
val res14 = useSqEval(5)
                      ^
Compilation Failed
```

This makes it much more explicit when we want to pass a memoized or lazy value around 
vs when we want to actually compute the value, which might take a long time.

Since [Eval][eval] is also a [Monad][monad], it can be used in a chain of computation 
without loosing the laziness of each function call.

```scala
@ def strSlowly(e: Int): Eval[String] = Eval.later {
     Thread.sleep(10000)
     e.toString
  }
defined function strSlowly

@ val both = for {
     sq <- squareSlowly(5)
     s <- strSlowly(sq)
    } yield s
both: Eval[String] = cats.Eval$$anon$4@66d0d5ee // immediate return

@ both.value
// ... 20 seconds later ...
res17: String = "25"
```

[lazy-val]: https://www.scala-exercises.org/scala_tutorial/lazy_evaluation
[by-name]: https://docs.scala-lang.org/tour/by-name-parameters.html
[memoization]: https://en.wikipedia.org/wiki/Memoization
[eval]: https://typelevel.org/cats/datatypes/eval.html
[monad]: https://typelevel.org/cats/typeclasses/monad.html

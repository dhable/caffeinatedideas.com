[Kleisli][kleisli]. It’s got a funny name but it’s actually a simple concept. Suppose you have 
two functions that you want to execute together:

```scala
val computeSquare(i: Int): Int = i * i
val formatResponse(i: Int): String = s"Your answer is $i"

// One way of doing it is
val sqrAns = computeSquare(3)
val strAns = formatResponse(sqrAns)  // strAns = "Your answer is 9"

// or avoiding naming variable
val strAns = formatResponse(computeSquare(3)).  // strAns = "Your answer is 9"
```

Instead of needing to call these two functions over and over, either as individual lines or nested, 
you can easily create a new function from the two that always does both using function composition:

```scala
val formattedSquare = computeSquare andThen formatResponse
// or
val formattedSquare = formatResponse compose computeSquare
```

Naturally, the output of one function flows as the input of the next function. We tend to run into 
problems through when our functions start to return monadic types like `Option` or `Either`

```scala
val computeSquare(i: Int): Either[Throwable, Int] = Right(i * i)
val formatResponse(i: Int): Either[Throwable, String] = 
   Right(s"Your answer is $i")
```

The output type of `computeSquare` no longer matches the input type of `formatResponse`. We could 
make them match but accepting an Either with one type being an error isn't a natural way to write 
a function and makes other uses of that function cumbersome.

The solution: [Kleisli][kleisli]

```scala
val computeSquare(i: Int): Either[Throwable, Int] = Right(i * i)
val formatResponse(i: Int): Either[Throwable, String] = 
   Right(s"Your answer is $i")

val formattedSquare = Kleisli(computedSquare) andThen Kleisli(formatResponse)

val ans = formattedSquare(3) // ans = Right("Your answer is 9")
```

The name isn't obvious but is nothing more than a way to compose when returned types are wrapped in another monadic type.

[kleisli]: https://typelevel.org/cats/datatypes/kleisli.html

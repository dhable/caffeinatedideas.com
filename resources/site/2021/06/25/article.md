Exceptions are the antithesis of functional programming since they
break referential transparency, the code after resolving the exception
cannot be substituted for the code prior to the exception. To avoid 
exceptions but still communicate errors to callers, we end up using
the `Either` type throughly through the code. We also want our code
to be async and so we also use the `Future` type in the result which
leads to many methods like:

```scala
case class Session(sessionId: String, userId: String)
case class User(userId: String, firstName: String, lastName: String)

def fetchSessionFromCache(
    sessionId: String
): Future[Either[Throwable, Session]] = 
    Future.successful(Right(Session(sessionId, "123")))

def fetchUserFromDb(
   userId: String
): Future[Either[Throwable, User]] = 
    Future.successful(Right(User(userId, "Some", "User")))
```

Trying to chain the methods with `for` starts to become fairly painful. It would be nice if
we could chain the method calls together without thinking about the `Future`. More generically,
we have two methods of type `F[Either[A, B]]` and we want to work with them as if they were only
of type `Either[A,B]`. This is where the [`EitherT`][1], aka either transform, type comes into play.

```scala
def greetCurrentUser(
    sessionId: String
)(implicit 
    ec: ExecutionContext
): Future[Either[Throwable, String]] = {
    val result: EitherT[Future, Throwable, String] = for {
            session <- EitherT(fetchSessionFromCache(sessionId))
            user <- EitherT(fetchUserFromDb(session.userId))
        } yield s"Hello ${user.firstName}"
    result.value
}
```

Using the constructor/apply, we can lift values from their nested type into
an EitherT and then manipulate them as if the outer type, `Future` in our example,
doesn't exist. When we're done with our operations, we can restore the nested types
with the `value` property.

[1]: https://typelevel.org/cats/datatypes/eithert.html

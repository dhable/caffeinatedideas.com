Let’s say I have two methods that return `Either` and I just want to take the 
first successful result, only defaulting to the error return if both fail. For example, each method
is going to look at different fields of a request body and if the required fields are present, build
an object.

```scala
trait Credential
case class CredType1(username: String, password: String) extends Credential
case class CredType2(token: String) extends Credential

case class ReqBody(username: Option[String], password: Option[String], token: Option[String])

def maybeCredType1(req: ReqBody): Either[Throwable, Credential] =
  Applicative[Option]
    .map2(req.username, req.password)(CredType1)
    .map(Right(_))
    .getOrElse(Left(new Exception("Cred1 requires both a username and password")))

def maybeCredType2(req: ReqBody): Either[Throwable, Credential] =
  req.token
    .map(value => Right(CredType2(value)))
    .getOrElse(Left(new Exception("Cred2 requires a token")))
```

I could write some additional logic that calls the correct method based on the values in `ReqBody` but then
I'd be duplicating the logic about which fields are required for which implementation of `Credential`. This is
where we want to call both and take the first success. There are a number of ways I could implement that, pattern 
matching, for comprehension, nested maps, but as I suspected this is a solved problem. Cats defines the `<+>` 
operator to implement this kind of alternative matching behavior.

```scala
def extractCredential(req: ReqBody): Either[Throwable, Credential] =
  maybeCredType1(req) <+> maybeCredType2(req)


extractCredential(ReqBody(Some("uname"), Some("pword"), None)
// res1 = Right(CredType1("uname", "pword"))

extractCredential(ReqBody(None, None, Some("token"))
// res2 = Right(CredType2("token"))

extractCredential(ReqBody(Some("uname"), None, None))
// res3 = Left(Throwable("Cred1 requires both a username and password"))

extractCredential(ReqBody(Some("uname"), None, Some("token"))
// res4 = Left(CredType2("token"))

extractCredential(ReqBody(None, None, None))
// res5 = Left(Throwable("Cred1 requires both a username and password"))
```

Now we can keep the logic on which request values are required for the types
of credentials inside individual constructor methods.

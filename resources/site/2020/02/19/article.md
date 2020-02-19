We often insist that anyone writing code understand and handle errors using exceptions. By their definition, exceptions 
force you to break your thought process on the present line of code and require that you immediately start to think about
how to handle the error condition. In some cases, like named exceptions in Java, there is no agency. You must either deal with
the exception or just declare it as a possible error to your code, pushing the concern to some other place in the code.

Exceptions ruin the flow of execution through a block of code. Take some Java code:

```java
public copyFile(File source, File dest) throws IOException {
    byte buf[4096];
    int bytesRead = -1;

    InputStream is = null;
    OutputStream os = null;
    try { 
        is = new FileInputStream(source);
        os = new FileOutputStream(dest);
        while ((bytesRead = is.read(buf)) > -1) {
            os.write(buf, bytesRead);
        }
    } finally {
        try {
            if (is != null) 
                is.close();
        } catch (IOException ex) {
            log.info("failed to close input stream", ex);
        }

        try {
            if (os != null) 
                os.close();
        } catch (IOException ex) {
            log.info("failed to close output stream", ex);
        }
    }
}
```

Which line(s) can throw an `IOException`? Well, pretty much all of them since a bulk of this code manipulates I/O 
streams. That doesn't even begin to discuss the possible `RuntimeExceptions` or even `Error` types that can result
in this code. When an `IOException` is thrown from this method, the developer has some idea what should have happened
but there is nothing explicit in our code to help us reason about the situation. Java 7+ tries to help us deal with
the situation by introducing the try-with-resource pattern. That helps us with trivial I/O errors but still leaves a
whole class of errors up to us.

What alternatives exist? Well, we could simply return an error state object. This is the approach that go has taken
to deal with errors.

```go
func copyFile(source string, dest string) (err error) {
    reader, err := os.Open(source)
    if err != nil {
        return err
    }

    defer reader.Close()

    writer, err := os.Create(dest)
    if err != nil {
        return err
    }

    defer writer.Close()

    buf := make([]byte, 4096);
    for {
        bytesRead, readErr := reader.Read(buf)
        if readErr != nil {
            if readErr != io.EOF {
                err = readErr
            } else {
                err = nil
            }
            break
        }
        if bytesRead > 0 {
            _, writeErr := writer.Write(buf[0:bytesRead])
            if writeErr != nil {
                err = writeErr
            }
        }
    }

    return err
}
``` 

This code is simply returning the error to the caller and asking them to deal with it. The key advantage here is that
we know at which points an error can be returned. If we were debugging this code, we could scan with our eyes to these
return points or places where `err` is set. In the Java example, we would have to read each line and try to guess if 
the exception could be raised by that particular line of code. If the method are well named, this can be fairly easy.
Ambiguous or ill-named methods then cause us to dive into another level of code.

Languages that are hosted on the JVM, like Scala, Kotlin or Clojure, need to break their functional paradigms of 
predictable execution in order to allow the interop to bubble Java exceptions into my pure functions. This seems like
a missed opportunity in the compilers to provide a standard wrapper around all interop code that transforms the exception
pattern into an [Either monad][either-monad] return and vice versa. Instead of making users of the language now opt-in
and possibly deal with two different paradigms of development, push the influence of exceptions into the interop boundary.

I want my JVM but pure functional programming too!

[either-monad]: https://hackage.haskell.org/package/category-extras-0.52.0/docs/Control-Monad-Either.html
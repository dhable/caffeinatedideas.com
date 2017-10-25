OpsCenter's goal is to deliver operational simplicity around deploying
and managing a DSE (e.g. Cassandra) cluster. One core feature
of this offering is backup and restore functionality that snapshots the
individual nodes and then copies that data to either AWS S3 or a local
file system mount. The component that performs this task, the DataStax agent,
simply views all the data as Java streams instead of individual files on 
the disk.

Since the agent abstracts everything as a stream, we do decompression of
files inline with the [`java.io.zip.GZipInputStream`][GZipInputStream] 
class. This leaves us needing to count the number of bytes after compression 
that have been transmitted in order to determine we didn't get interrupted
during the restore. For that purpose, we used Clojure to extend
the [`java.io.FilterInputStream`][FilterInputStream] class and made our own 
byte counting stream. 

```clojure
(defn byte-counting-stream
  [input-stream]
  (let [counter (atom 0)
        update-fn (fn [x] (swap! counter #(+ % x)))]        
    [(proxy [FilterInputStream]
           [input-stream]
      (read
        ([]
         (let [result (.read input-stream)]
           (when (> result -1)
             (update-fn 1))
           result))

        ([^bytes bytes]
         (let [result (.read input-stream bytes)]
           (when (> result -1)
             (update-fn result))
           result))

        ([^bytes bytes ^long off ^long len]
         (let [result (.read input-stream bytes off len)]
           (when (> result -1)
             (update-fn result))
           result))))
     counter]))
```

Astute readers will note that while functionally correct, the subclass fails to call the
parent class version of read, which would need to be done with `(proxy-super read)`. As 
written, this code fails to call the parent class and might miss some additional functionality. 
While that is a failure of this code, [`FilterInputStream`][FilterInputStream] does nothing 
but forward the call to the object it wrapped making the code functionally the same. That isn't 
something I caught  until looking at the implementation much later.

Instead this code was removed because of the funky return type. It returns a 2-element vector with
the wrapped stream instance as the first element and the Clojure `atom` with the byte count
as the second element. This return type made it difficult to deal with resource cleanup on
exceptions since the `with-close` macro was not designed to work with this type. As part of
a major release, I ended up replacing it with [Guava's][guava] [`CountingInputStream`][CountingInputStream].


## The Effect of Guava's Replacement   

One of the goals of this major release was to address backup and restore performance in
OpsCenter. To measure our success, the test engineering group created and ran a series
of performance test suites to measure the effects between releases.

```markdown
                6.0.7       6.1.0
                ------      ------
S3 Backup       47 min      35 min
S3 Restore      920 min     63 min
```

What we discovered was that the overall efforts in the release did increase performance
and that the biggest gain of 850 minutes was realized in the restore path. Success was
declared but the nagging feeling of what caused that level of performance gain lingered
in my mind. 


## Hypothesis: The Clojure atom 

I kept returning to the use of an atom as a counter. Didn't atoms acquire a lock
when updated to ensure correctness of the values? The move to [`CountingInputStream`][CountingInputStream]
simply removed the unnecessary lock and thus where we had a huge performance gain. I
decided to isolate this code in a [simple performance test suite][repo] that generated huge
streams of constant values to avoid GC pressures. I then created a simple test runner
that would read streams of various sizes and track the wall time required to read
through the entire stream.

The various implementations I wanted test included a benchmark of the test without any
byte counting on the stream, the original code implementation as listed above, the
[Guava][guava] implementation and a implementation that used a Java counter. The Java counter
was simply a class that wrapped a long and had an increment method. If my hypothesis
was correct, the Java counter should perform as well as the [Guava][guava] implementation.

Here were the results of running those test cases.

```markdown
Test Stream Sizes: 5 MB / 50 MB / 500 MB
====================================================

>>> No Counting                =>    264 ms /   2581 ms /  26551 ms
>>> Guava Stream Impl          =>    249 ms /   2545 ms /  26123 ms
>>> OpsC Code - Atom           =>   9643 ms /  95588 ms / 931874 ms
>>> Java Counter               =>   9023 ms /  89072 ms / 878795 ms
```

Immediately you can see that the [Guava][guava] implementation in Java is about as performant
as a non-counted stream and that the Clojure implementation using an atom is quite
slow. This all matches the performance benchmark results we saw with the black box 
testing done between releases. The real shocking result was the slow performance of
the Java counter implementation - it's nearly as slow as the atom! Numerous test runs
produced the same results. 

The next step was to run this test suite with [Visual VM][VisualVM] and observe what is happening
in the JVM as the Java counter test is running. Trying to do so on the agent itself
yields a lot of noise as the Clojure runtime metrics are mixed into your application
metrics. IMO, this is a weakness to using an interpreted language on the JVM. Not 
surprisingly the amount of time spent in reflection calls starts to dominate the execution
time. I still don't know why I don't see warnings about these reflection calls even though
I have `*warn-on-reflection*` set in the application.


## Fixing the Reflection

After a bit of fiddling with atom byte counting implementation, I found that a single
type hint of `^InputStream` in the function argument list totally changes the performance
of the code. Here are the results of running the same tests with type hinted implementations.

```markdown
Test Stream Sizes: 5 MB / 50 MB / 500 MB
====================================================

>>> Type Hinted OpsC Atom      =>    776 ms /   7335 ms /  69981 ms
>>> Type Hinted Java Counter   =>    405 ms /   3954 ms /  40609 ms
```

The execution time for these two implementations is now much more reasonable than before
but still nowhere near as performant as the [Guava][guava] implementation. Some amount of time does
seem to be consumed by the atomic updates to the atom as highlighted by the Java counter
test case. The remainder of the time is simply the overhead of the Clojure runtime produced
object instance. As far as I can tell, the proxy macro doesn't compile down to bytecode 
before execution and thus runs with more overhead that is unavoidable. Just on a whim, I decided
to write my own `ByteCountingInputStream` in obvious Java (with a lot of help from IntelliJ).

```markdown
Test Stream Sizes: 5 MB / 50 MB / 500 MB
====================================================

>>> Custom Java Stream Impl    =>    255 ms /   2616 ms /  26600 ms
```

The performance here is spot on with the [Guava][guava] implementation. There isn't any compile time
optimizations or tricks in [Guava][guava]. Clojure objects just seem to perform slower than the 
equivalent Java code after it's compiled down to bytecode.


## Conclusion

Writing the most obvious byte counting stream implementation in Java is quicker to code and
more performant than the Clojure solution.

The interesting part of this use case is the forced atomic updates to a mutable variable
that Clojure forces upon you even though it's not necessary. The [`java.io.InputStream`][InputStream] 
methods have been unsafe for multi-threaded access since Java 1.0 and is a core assumption in the 
interface made. Imposing the atomic update, while not functionally incorrect, does impose
changes to the behavioral parts of the API. I don't have a good approach for how to write
this kind of logic in pure Clojure and retain the performance of the Java. The answer seems to
be that as a Clojure developer, you must know when you need to drop into Java for logic and
be fluent enough in Java to produce code in both languages.

I suspect that this type of use case, extending low level Java lib constructs, is always going
to be better suited for Java code instead of Clojure. Where Clojure really outweighs Java is
in the transformation of data structures or expressing functional concepts on streams of data.
I don't think it's possible to maintain a "pure" Clojure codebase if the code needs to perform
at scale. How to impart that JVM knowledge and decision making to Clojure developers who come
to the platform not from the JVM side but from other dynamic languages is something I think
needs more discussion.

[repo]: https://github.com/dhable/byte-counter-perf
[GZipInputStream]: https://docs.oracle.com/javase/8/docs/api/java/util/zip/GZIPInputStream.html
[FilterInputStream]: https://docs.oracle.com/javase/8/docs/api/java/io/FilterInputStream.html
[guava]: https://github.com/google/guava
[CountingInputStream]: http://google.github.io/guava/releases/23.0/api/docs/com/google/common/io/CountingInputStream.html
[VisualVM]: https://visualvm.github.io/
[InputStream]: https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html

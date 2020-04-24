One of the many things that OpsCenter had to do was spawn new processes
with custom environment variables using the `environment()` 
method on the [ProcessBuilder][proc-builder] class. The method, which embraces mutable
state, returns a `Map<String, String>` that you need to update with whatever
new values you want to pass through to the new process.
```clojure
(defn start-application []
  (let [cmd (into-array String ["java" "-jar" "App.jar"])
        proc-builder (ProcessBuilder. cmd)
        env (.environment proc-builder)]
    (.put env "ENV_NAME" "dev")
    (.start proc-builder)))
```
The `start-application` function spawns a new Java application while setting the
`ENV_NAME` parameter for the local machine and returns the new [`Process`][proc] object.

Simple process creation is probably easy enough to do in a single function
declaration but spawning real processes probably means a bunch of conditional
logic around which environment variables to set. We solved this structure problem
by creating a second function that would accept the environment map and add the
appropriate elements to the map.
```clojure
(defn set-env-vars [env]
   ;; This would typically be more complex 
   (.put env "ENV_NAME" "dev"))

(defn start-application []
  (let [cmd (into-array String ["java" "-jar" "App.jar"])
        proc-builder (ProcessBuilder. cmd)
        env (.environment proc-builder)]
    (set-env-vars env)
    (.start proc-builder)))
```
We found that this worked fine but when we started trying to run this code in Oracle JDK 11,
the code would fail with a cryptic message about no put on `env`. The `ProcessBuilder.environment()` method
simply makes a call to the `ProcessEnvironment` class, which has the following implementation:
```java
final class ProcessEnvironment extends HashMap<String,String>
{
    // ...

    public String put(String key, String value) {
        return super.put(validateName(key), validateValue(value));
    }
    // ...

    // Only for use by ProcessBuilder.environment()
    @SuppressWarnings("unchecked")
    static Map<String,String> environment() {
        return (Map<String,String>) theEnvironment.clone();
    }

    // ...
}
```
The `environment()` method creates the new map returned from `ProcessBuilder.environment()`. You can
see that `ProcessEnviromnet` extends `HashMap`, which is how it gains the polymorphic behavior of `Map`.
The unusual part of the definition is that the subclass extends a version of `HashMap<String, String>` that
includes the type parameters filled in. This means that the compiler knows that `String put(String, String)`
does indeed fulfill the contract of the base class.

The error we were running into stems from Clojure's reflection use. Java erases the type parameters when it
compiles into byte code thus Clojure had no idea that the instance of `ProcessEnvironment` was a Map that only
had implementations for Strings. Adding type hints in Clojure fixed the problem.

[proc-builder]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/ProcessBuilder.html
[proc]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Process.html
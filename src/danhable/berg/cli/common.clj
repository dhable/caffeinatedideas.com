(ns danhable.berg.cli.common
  "Collection of useful functions to make it easier to write consistent CLI commands.")


(defmacro timed
  "Like cloure.core/time but instead of printing off the timing with prn, returns a map
  with the form result (:ret) and the nanoseconds taken to execute (:time)."
  [form]
  `(let [start# (System/nanoTime)
         ret# ~form
         end# (System/nanoTime)]
     {:ret ret# :time (- end# start#)}))


(defmacro exec>
  "Executes expr timed and then prints to standard out msg, whether expr ended
  successfully or raised and exception and the milliseconds taken to execute."
  [msg expr]
  `(do
     (print ~msg) (flush)
     (try
       (let [ret# (timed ~expr)]
         (println (format " DONE [%f ms]" (/ (double (:time ret#)) 1000000.0)))
         (:ret ret#))
       (catch Exception e#
         (println " ERROR!")
         (throw e#)))))

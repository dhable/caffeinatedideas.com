(ns danhable.berg.page
  "Contains the Page data structure that represents an EDN page file on disk along with
  all of the associated functions to manipulate a Page. These are the most primitive data
  structure in the code base."
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clj-time.format :as time-format]
            [markdown.core :as markdown]
            [danhable.berg.io :as io+]))


(defrecord Page [source-file      #_"java.io.File instance pointing to EDN source"
                 url-path         #_"string pointing to uri destination (becomes target file name later)"
                 template-name    #_"string containing the template that can render this"
                 resources        #_"list of additional static files in the same dir as source that need to be included"
                 data             #_"dict of custom values for this page"
                 rendered-view    #_"string containing application of the template" ])


(defmulti include-external-content (fn [base-dir filename]
                                     (.toLowerCase (io+/get-file-extension filename))))

(defmethod include-external-content ".md"
  [base-dir filename]
  (-> (io/file base-dir filename)
      slurp
      (markdown/md-to-html-string :reference-links? true
                                  :footnotes? true)))

(defmethod include-external-content :default
  [base-dir filename]
  (slurp (io/file base-dir filename)))


(defn read-page-content
  "Reads a page EDN file, f, from disk with custom reader macros defined and returns
  the contents as a Clojure data structure."
  [f]
  (let [base-dir (.getParent f)
        custom-reader-macros {'include (partial include-external-content base-dir)
                              'date (partial time-format/parse (time-format/formatter "yyyy-MM-dd"))}]
    (try
      (with-open [reader (io+/pushback-reader f)]
        (edn/read {:readers custom-reader-macros} reader))
      (catch Exception e
        (println "Failed to read content for page " (str f) ". Reason: " e)
        (throw e)))))


(defn is-page-source?
  "Predicate function to determine whether a java.nio.files.Path instance references
  a valid site source file. Valid site sources have a .edn extension."
  [path]
  (if path
    (.. path getFileName toString (endsWith ".edn"))
    false))


(defn get-resource-files
  [src-file res-list]
  (map #(io/file (.getParent src-file) %) res-list))


(defn write-resource-file
  [page resource target-dir]
  (let [resource-name (.getName resource)
        dest-dir (io/file target-dir (.getParent (:url-path page)))
        dest-resource (io/file dest-dir resource-name)]
    (io+/touch! dest-resource)
    (io/copy resource dest-resource)))


(defn write-rendered-page
  [page target-dir]
  (let [target-file (io/file target-dir (:url-path page))]
    (io+/touch! target-file)
    (spit target-file (:rendered-view page)))
  (doseq [resource (:resources page)]
    (write-resource-file page resource target-dir)))


(defn generate-relative-page-url
  [src-file rel-base]
  (as-> src-file $
        (io+/relativize rel-base $)
        (.toFile $)
        (io+/replace-file-extension $ ".html")))


(defn new-Page
  "Given a page File object, f, create a new instance of the Page record by reading the
  contents of the file and pulling the data apart into the correct fields."
  [base-dir f]
  (let [content (read-page-content f)]
    (map->Page {:source-file   f
                :url-path      (generate-relative-page-url f base-dir)
                :template-name (get content :template-name)
                :resources     (get-resource-files f (get content :resources))
                :data          (get content :data)
                })))


(defn load-all-pages
  "Given a base directory, recursivly looks for all EDN files and creates Page objects
  for them."
  [base-dir]
  (map (partial new-Page base-dir) (io+/list-files base-dir :recursive? true
                                                            :filter is-page-source?)))

(ns danhable.berg.page
  "Contains the Page data structure that represents an EDN page file on disk along with
  all of the associated functions to manipulate a Page. These are the most primitive data
  structure in the code base."
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s]
            [danhable.berg.commonmark :as commonmark]
            [danhable.berg.io :as io+])
  (:import [java.time.format DateTimeFormatter]
           [java.time LocalDate]))


(s/defschema PageType
  "Defines a map object containing all the attributes that represent a source page
  from disk. This is what's translated into an output page when compiled."
  {:source-file io+/FileType
   :url-path io+/FileType
   :template-name s/Str
   :resources (s/maybe [s/Str])
   :data s/Any ;; Opaque content that is consumed by the individual template pages
   (s/optional-key :rendered-view) (s/maybe s/Str)})


(defmulti include-external-content (fn [base-dir filename]
                                     (.toLowerCase (io+/get-file-extension filename))))

(defmethod include-external-content ".md"
  [base-dir filename]
  (-> (io/file base-dir filename)
      slurp
      commonmark/md-to-html-string))

(defmethod include-external-content :default
  [base-dir filename]
  (slurp (io/file base-dir filename)))

(s/defn date-reader :- LocalDate
  [value :- s/Str]
  (LocalDate/parse value DateTimeFormatter/ISO_LOCAL_DATE))

(s/defn read-page-content :- s/Any
  "Reads a page EDN file, f, from disk with custom reader macros defined and returns
  the contents as a Clojure data structure."
  [f :- io+/FileType]
  (let [base-dir (.getParent f)
        custom-reader-macros {'include (partial include-external-content base-dir)
                              'date date-reader}]
    (try
      (with-open [reader (io+/pushback-reader f)]
        (edn/read {:readers custom-reader-macros} reader))
      (catch Exception e
        (println "Failed to read content for page " (str f) ". Reason: " e)
        (throw e)))))


(s/defn is-page-source? :- s/Bool
  "Predicate function to determine whether a java.nio.files.Path instance references
  a valid site source file. Valid site sources have a .edn extension."
  [path :- (s/maybe io+/PathType)]
  (if path
    (.. path getFileName toString (endsWith ".edn"))
    false))


(s/defn get-resource-files :- (s/maybe [io+/FileType])
  [src-file :- io+/FileType, res-list :- (s/maybe [s/Str])]
  (let [parent-dir (.getParent src-file)]
    (map #(io/file parent-dir %) res-list)))


(s/defn write-resource-file :- nil
  [page :- PageType, resource :- io+/FileType, target-dir :- io+/FileType]
  (let [resource-name (.getName resource)
        dest-dir (io/file target-dir (.getParent (:url-path page)))
        dest-resource (io/file dest-dir resource-name)]
    (io+/touch! dest-resource)
    (io/copy resource dest-resource)))


(s/defn write-rendered-page :- nil
  [page :- PageType, target-dir :- io+/FileType]
  (let [target-file (io/file target-dir (:url-path page))]
    (io+/touch! target-file)
    (spit target-file (:rendered-view page)))
  (doseq [resource (:resources page)]
    (write-resource-file page resource target-dir)))


(s/defn generate-relative-page-url :- io+/FileType
  [src-file :- io+/FileType, rel-base :- io+/FileType]
  (as-> src-file $
        (io+/relativize rel-base $)
        (.toFile $)
        (io+/replace-file-extension $ ".html")))


(s/defn new-Page :- PageType
  "Given a page File object, f, create a new instance of the Page record by reading the
  contents of the file and pulling the data apart into the correct fields."
  [base-dir :- io+/FileType, f :- io+/FileType]
  (let [content (read-page-content f)]
    {:source-file   f
     :url-path      (generate-relative-page-url f base-dir)
     :template-name (get content :template-name)
     :resources     (get-resource-files f (get content :resources))
     :data          (get content :data)}))


(s/defn load-all-pages :- [PageType]
  "Given a base directory, recursively looks for all EDN files and creates Page objects
  for them."
  [base-dir :- io+/FileType]
  (map (partial new-Page base-dir) (io+/list-files base-dir :recursive? true
                                                            :filter is-page-source?)))

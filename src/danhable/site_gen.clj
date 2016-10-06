(ns danhable.site-gen
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [markdown.core :as md]
            [boot.core :as core]
            [clj-time.format :as format])
  (:import [java.io File
                    PushbackReader]))

(def ^:const METADATA-FILENAME "meta.edn")
(def ^:const POST-FILENAME "post.md")

(def not-nil? (comp not nil?))

(defn generate-dir-list
  [dir]
  (let [dir-obj (io/as-file dir)]
    (when-not (or (nil? dir-obj)
                  (.isFile dir-obj))
      (->> dir-obj
           (.listFiles)
           (filter #(.isDirectory %))))))

(defn pushback-reader!
  [& args]
  (let [file-obj (apply io/file args)]
    (PushbackReader. (io/reader file-obj))))

(defn read-meta-data
  [^File dir]
  (with-open [meta-data-stream (pushback-reader! dir METADATA-FILENAME)]
    (edn/read meta-data-stream)))

(defn get-resource-file-list
  [^File dir]
  (->> (io/as-file dir)
       (.listFiles)
       seq
       (filter #(.isFile %))
       (filter #(not= (.getName %) METADATA-FILENAME)) ;; Can these two filter statements be combined?
       (filter #(not= (.getName %) POST-FILENAME))))

(defn build-post-data
  [^File dir]
  (let [post-dir-matcher (re-matcher #"^(\d{4}-\d{2}-\d{2})-([\w-]+)$" (.getName dir))
        publish-date-formatter (format/formatter "yyyy-MM-dd")
        [_ publish-date page-slug] (re-find post-dir-matcher)]
  {:metadata (read-meta-data dir)
   :source (io/file dir POST-FILENAME)
   :resources (get-resource-file-list dir)
   :publish-date (format/parse publish-date-formatter publish-date)
   :page-slug page-slug}))

(defn nav-partition
  [s]
  (conj (filter #(not-nil? (second %))
                (partition 3 1 [nil nil] s))
        (list nil (first s) (second s))))

(defn get-title-and-url
  [page-data]
  (if-not (nil? page-data)
    {:title (get-in page-data [:metadata :title])
     :url (get-in page-data [:metadata :target-page])}
    nil))

(defn build-nav-context
  [page-data-seq]
  (map #(let [[prev page next] %]
          (-> page
              (assoc-in [:context :nav :prev] (get-title-and-url prev))
              (assoc-in [:context :nav :next] (get-title-and-url next))))
       (nav-partition page-data-seq)))

(defn build-page-context
  [page-data-seq]
  (map (fn [page-data]
         (assoc-in page-data
                   [:context :nav :page]
                   (get-in page-data [:metadata :tags])))
       page-data-seq))

(defn load-posts
  [dir]
  (->> (generate-dir-list dir)
       (map build-post-data)
       (sort-by :publish-date)
       (filter #(not (get-in % [:metadata :draft?] false)))
       build-nav-context
       build-page-context))


;; Processing Pipeline For Site:
;;  1. Load all posts from disk into seq of map data
;;     a. generate list of all possible posts from directory data [done]
;;     b. transform directory data into map data structure        [done]
;;     c. sort by date to publish                                 [done]
;;     d. (optional) filter out drafts                            [done]
;;     e. build page and nav page context values                  [done]
;;
;;  2. Load all pages from disk into seq of map data
;;     a. Find all .html and .edn files that are children of /pages
;;     b. generate map page data objects
;;        i. if file is .html then just source / dest paths
;;        ii. if file is .edn then load edn into metadata,
;;            mangle edn filename into dest page path
;;
;;  3. Resolve all pages and post data objects using the
;;     templates to disk. Copy resources too.

(core/deftask site-gen
  ""
  [p post-dir     DIR str "The post source directory"
   s site-dir     DIR str "The compiled site directory"
   t template-dir DIR str "The template directory"]

  ;; TODO: Should I log these debug statements or just drop them
  (println (str "post-dir value is: " post-dir))
  (println (str "site-dir value is: " site-dir))

  (let [posts (load-posts post-dir)
        pages [] ;; load pages?
        ]

    (pprint posts)

  )
)

(ns danhable.site-gen
  (:require [clojure.spec :as spec]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [markdown.core :as md]
            [boot.core :as core]
            [boot.task.built-in :as task]))

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
    (java.io.PushbackReader. (io/reader file-obj))))

(defn read-meta-data
  [^java.io.File dir]
  (with-open [meta-data-stream (pushback-reader! dir METADATA-FILENAME)]
    (edn/read meta-data-stream)))

(defn get-resource-file-list
  [^java.io.File dir]
  (->> (io/as-file dir)
       (.listFiles)
       seq
       (filter #(.isFile %))
       (filter #(not= (.getName %) METADATA-FILENAME))
       (filter #(not= (.getName %) POST-FILENAME))))

(defn build-post-data
  [^java.io.File dir]
  {:metadata (read-meta-data dir)
   :publish-date (java.util.Date.) ;; TODO: replace with datetime
   :source (io/file dir POST-FILENAME)
   :resources (get-resource-file-list dir) })

(defn nav-partition
  [s]
  (conj (filter #(not-nil? (second %))
                (partition 3 1 [nil nil] s))
        (list nil (first s) (second s))))

(defn get-map-and-url
  [page-data]
  (if-not (nil? page-data)
    {:title (get-in page-data [:metadata :title])
     :url (get-in page-data [:metadata :target-page])}
    nil))

(defn build-nav-context
  [page-data-seq]
  (map #(let [[prev page next] %]
          (-> page
              (assoc-in [:context :nav :prev] (get-map-and-url prev))
              (assoc-in [:context :nav :next] (get-map-and-url next))))
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

;;--------------------

; (defn make-target-dir! ;; TODO: needed?
;   [post-data]
;   (let [{{:keys [target-dir]} :metadata} post-data
;         target-dir-obj (apply io/file target-dir)]
;     (io/make-parents target-dir-obj)
;     (assoc-in post-data [:metadata :target-dir] target-dir-obj)))
;
;
; (defn compile-post! ;; TODO: needed?
;   ""
;   [post-data]
;   (let [{:keys [metadata post]} post-data
;         {:keys [target-dir target-page]} metadata
;         target-file-obj (apply io/file (conj target-dir target-page))]
;     (md/md-to-html post target-file-obj)
;     (assoc post-data :post-target target-file-obj)))
;
;
; (defn copy-resource-files! ;; TODO: needed
;   ""
;   [post-data]
;   (let [{:keys [metadata resources]} post-data
;         resource-target-base (:target-dir metadata)
;         target-resource-objs (map #(io/file (.getParent resource-target-base)
;                                             (.getName %)))]
;     (doseq [[source dest] (map list resources target-resource-objs)]
;       (io/copy source dest))
;     (assoc post-data :target-resources target-resource-objs)))


;; Processing Pipeline For Site:
;;  1. Load all posts from disk into seq of map data
;;     a. generate list of all possible posts from directory data [done]
;;     b. transform directory data into map data structure        [done]
;;     c. sort by date to publish                                 [sort of]
;;     d. (optional) filter out drafts                            [done]
;;     e. build page and nav page context values                  [done]
;;
;;  2. Load all pages from disk into seq of map data
;;
;;  3. Compile blog data structure into files on disk

(core/deftask site-gen
  ""
  [p post-dir DIR     str             "The post source directory"
   s site-dir DIR     str             "The compiled site directory"]

  ;; TODO: Should I log these debug statements or just drop them
  (println (str "post-dir value is: " post-dir))
  (println (str "site-dir value is: " site-dir))

  (let [posts (load-posts post-dir)
        pages []
        templates []]

    (pprint (map #(get-in % [:metadata :title]) posts))

  )
)

(ns danhable.berg.theme
  "Contains logic to manipulate and apply a theme to a Page. A Theme is stored on disk
  as a directory, where the directory name is the name of the Theme, which contains two
  special subdirectories:

    templates - a single level directory that contains HTML template files. The filename
                without the extension is the name of the template. HTML files that start
                with an underscore are considered private and will not be loaded into the
                Theme record. Private files are designed to be included from other templates.

    static - anything in this directory, including subdirectories, will be considered a
             static artifact for site construction and will be copied as is."
  (:require [clojure.java.io :as io]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]
            [danhable.berg.io :as io+]))


(defrecord Theme [name base-dir templates static-files])


(defn is-private-template-file?
  "Predicate function that returns true if path is a java.nio.file.Path to a
  private teamplate file. These are files that start their filename with an
  underscore and are only used as imports from other templates."
  [path]
  (if path
    (.. path getFileName toString (startsWith "_"))
    false))


(defn template-name
  "Given a File instance, returns the template name. This is currently the name
  of the file with any extension stripped from it."
  [f]
  (when f
    (-> (.getName f) io+/trim-extension)))


(defn load-templates
  "From a base-dir, find all of the non-private template files and return a hashmap of
  template names and opaque blobs that are enough details for apply-to-page to produce
  rendered output."
  [base-dir]
  (selmer-util/set-custom-resource-path! (.toURI base-dir))
  (->> (io+/list-files base-dir :recursive? false :filter (comp not is-private-template-file?))
       (map #(vector (template-name %1)
                     (selmer/parse selmer/parse-file (.getName %1) {})))
       (into {})))


(defn load-static-files
  [base-dir]
  (let [base-path (.toPath base-dir)]
    (->> (io+/list-files base-dir :recursive? true)
         (map #(vector (str (io+/relativize base-path %1)) %1))
         (into {}))))


(defn apply-to-page
  "Apply a theme to a page if the template defined in the page data is part of this theme. Returns
  the result of this application such that it can be written to output files."
  [theme page context]
  (let [template-name (:template-name page)
        template-node (get (:templates theme) template-name)]
    (selmer/render-template template-node context)))


(defn copy-static-files
  [theme target-dir]
  (doseq [[relpath f] (:static-files theme)]
    (let [target-file (io/file target-dir relpath)]
      (io+/touch! target-file)
      (io/copy f target-file))))


(defn new-Theme
  "From a theme's base directory, read through all the files and create a new Theme
  record."
  [base-dir]
  (map->Theme {:name (.getName base-dir)
               :base-dir base-dir
               :templates (load-templates (io/file base-dir "templates"))
               :static-files (load-static-files (io/file base-dir "static"))}))

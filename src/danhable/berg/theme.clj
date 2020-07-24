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
            [schema.core :as s]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]
            [danhable.berg.io :as io+]
            [danhable.berg.theme.filters :as theme-filters]))


(s/defschema TemplateNameType
  "Defines a valid template name. Valid template names are filename
   strings that do not start with underscore (_) or contain any
   extensions."
  (s/constrained s/Str #(re-matches #"^[a-zA-Z0-9]\w*$" %) "Does not match template name pattern"))


(s/defschema TemplateMapType
  "Defines a mapping between a template name and the template content. The
   template content is an opaque string."
  {TemplateNameType s/Any})


(s/defschema StaticMapType
  "Defines a mapping between a relative file path string and a file object.
   The relative mapping is used to determine where in the compiled output
   to copy the data referenced by the File value."
  {s/Str io+/FileType})


(s/defschema ThemeType
  "Defines a map that is the aggerate of the theme data."
  {;; The name of the template, which also happens to be the name of the template directory
   :name s/Str
   ;; Reference to the template directory
   :base-dir io+/FileType
   ;; A map of template names (filenames that do not start with understore, no extensions) and the template content
   :templates (s/maybe TemplateMapType)
   ;; A map of relative static file names and reference to the file
   :static-files (s/maybe StaticMapType)})


(s/defn is-private-template-file? :- s/Bool
  "Predicate function that returns true if path is a java.nio.file.Path to a
  private template file. These are files that start their filename with an
  underscore and are only used as imports from other templates."
  [path :- (s/maybe io+/PathType)]
  (if path
    (.. path getFileName toString (startsWith "_"))
    false))


(s/defn template-name :- (s/maybe s/Str)
  "Given a File instance, returns the template name. This is currently the name
  of the file with any extension stripped from it."
  [f :- (s/maybe io+/FileType)]
  (when f
    (io+/trim-extension (.getName f))))


(s/defn load-templates :- (s/maybe TemplateMapType)
  "From a base-dir, find all of the non-private template files and return a hashmap of
  template names and opaque blobs that are enough details for apply-to-page to produce
  rendered output."
  [base-dir :- io+/FileType]
  (when (.exists base-dir)
    (selmer-util/set-custom-resource-path! (.toURI base-dir))
    (->> (io+/list-files base-dir :recursive? false :filter (comp not is-private-template-file?))
         (map #(vector (template-name %1)
                       (selmer/parse selmer/parse-file (.getName %1) {})))
         (into {}))))


(s/defn load-static-files :- (s/maybe StaticMapType)
  "Given a base-dir, recursively looks at all files and returns a map of those files.
  The keys are the path relative to the base-path and the values are java.io.File objects
  for the absolute files."
  [base-dir :- io+/FileType]
  (when (.exists base-dir)
    (let [base-path (.toPath base-dir)]
      (->> (io+/list-files base-dir :recursive? true)
           (map #(vector (str (io+/relativize base-path %1)) %1))
           (into {})))))


(s/defn apply-to-page :- s/Str
  "Apply a theme to a page if the template defined in the page data is part of this theme. Returns
  the result of this application such that it can be written to output files."
  [theme :- ThemeType, page :- s/Any,  context :- s/Any]
  (let [template-name (:template-name page)
        template-node (get (:templates theme) template-name)]
    (selmer/render-template template-node context)))


(s/defn copy-static-files :- nil
  "Given a Theme record and a target-dir directory, this function copies all of the static files
  on the Theme object to target-dir and building the relative path for the files in target-dir
  to ensure the end directory structure matches the source structure. Returns nil."
  [theme :- ThemeType, target-dir :- io+/FileType]
  (doseq [[relpath f] (:static-files theme)]
    (let [target-file (io/file target-dir relpath)]
      (io+/touch! target-file)
      (io/copy f target-file))))


(s/defn new-Theme :- ThemeType
  "From a theme's base directory, read through all the files and create a new Theme
  record."
  [base-dir :- io+/FileType]
  (theme-filters/load-custom-filters!)
  {:name (.getName base-dir)
   :base-dir base-dir
   :templates (load-templates (io/file base-dir "templates"))
   :static-files (load-static-files (io/file base-dir "static"))})

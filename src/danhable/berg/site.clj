(ns danhable.berg.site
  "
    site map?
    table of contents - listing of where everything is in the site

      what data structure makes sense here? tree? list?

       Tuple( [section1, section2, section3], Page )

      sort (list of tuples) -> list of tuples

      slice (list of tuples, [sections to select on]) -> list of matching tuples

      apply-to (list of tuples, some-fn) -> list of results

      path->TOCEntry (url-path) -> Tuple(...)
        creates a tuple structure from a url path

      TOCEntry->path (Tuple) -> url path


      Open Decisions:
      [design; hard]
        - How do I want to handle site content stuff (title, next page, prev page, etc)?

        [design; hard]
        - How do I want to design things like RSS feed generation?
          special, non-HTML template?

        [design; hard]
        - Working with images is very much undefined at this point, should work for attachements in general
      "
  (:require [clojure.java.io :as io]
            [danhable.berg.page :as page]
            [danhable.berg.theme :as theme]))


(defrecord Site [pages theme conf target-dir])


(def ^:private default-site-conf {:title "Default Site Title"
                                  :description ""
                                  :author ""
                                  :site-url ""
                                  :date-format ""
                                  :theme "resources/themes/default"
                                  :sources "resources/site"
                                  :target "target/site"})


(defn new-Site
  ""
  [options]
  (let [site-conf (as-> options $
                        (merge default-site-conf $)
                        (select-keys $ (keys default-site-conf)))
        theme-dir (io/as-file (:theme site-conf))
        source-dir (io/as-file (:sources site-conf))
        target-dir (io/as-file (:target site-conf))]
    (map->Site {:conf site-conf
                :target-dir target-dir
                :pages (page/load-all-pages source-dir)
                :theme (theme/new-Theme theme-dir)})))


(defn compile-site
  [site]
  (let [{:keys [pages theme]} site]
    (->> pages
         (map #(assoc %1 :rendered-view (theme/apply-to-page theme %1)))
         (assoc site :pages))))


(defn write-to-disk
  [site]
  (let [target-dir (:target-dir site)]
    (theme/copy-static-files (:theme site) target-dir)
    (doseq [p (:pages site)]
      (page/write-to-disk p target-dir))))
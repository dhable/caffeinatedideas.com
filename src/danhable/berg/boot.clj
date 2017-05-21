(ns danhable.berg.boot
  ""
  (:require [clojure.java.io :as io]
            [boot.core :refer [deftask] :as boot]
            [danhable.berg.site :as site]))

(deftask assemble
  "Generates static site output from source files and themes."
  [s source DIR str "Where the site source files are located"
   t theme  DIR str "Base directory of the template to apply"
   o output DIR str "The output or target directory for the site"]
  (let [site-target-dir (io/as-file output)]
    (boot/empty-dir! site-target-dir)
    (boot/with-pre-wrap
      [fs]
      (-> (site/new-Site {:theme theme
                          :sources source
                          :target site-target-dir})
          site/compile-site
          site/write-to-disk)
      (-> fs (boot/add-resource site-target-dir) boot/commit!))))

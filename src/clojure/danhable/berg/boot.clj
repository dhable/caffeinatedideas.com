(ns danhable.berg.boot
  ""
  (:require [clojure.java.io :as io]
            [boot.core :refer [deftask] :as boot]
            [danhable.berg.site :as site]))

;(deftask generate-site
;   ""
;   [p post-dir     DIR str "The post source directory"
;    g page-dir     DIR str "The page source directory"
;    s site-dir     DIR str "The compiled site directory"
;    t template-dir DIR str "The template directory"]
;

(deftask assemble
  "Generates static site output from source files and themes."
  []
  (let [site-target-dir (io/as-file "target")] ;; TODO: should take these options in from build.boot
    (boot/empty-dir! site-target-dir)
    (boot/with-pre-wrap
      [fs]
      (-> (site/new-Site {:theme "src/themes/default"
                          :sources "src/site"
                          :target site-target-dir})
          site/compile-site
          site/write-to-disk)
      (-> fs (boot/add-resource site-target-dir) boot/commit!))))


(ns danhable.berg.cli.assemble
  "Implementation of the command line utility for assembling the site from source files and a template."
  (:require [clojure.java.io :as io]
            [danhable.berg.site :as site]
            [danhable.berg.cli.common :refer [exec>]])
  (:import [org.apache.commons.io FileUtils]))


(def site-options {:title "Caffeinated Ideas"
                   :description ""
                   :author "Dan Hable"
                   :site-url "http://caffeinatedideas.com"
                   :date-format "YYYY MMM dd"
                   :theme "resources/themes/default"
                   :sources "resources/site"
                   :target "target"})


(defn -main [& _]
  (exec> "Cleaning prior assembled site files..." (FileUtils/deleteDirectory (io/as-file (:target site-options))))
  (exec> "Assembling site from source..." (site/assemble-site site-options)))

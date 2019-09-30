(ns danhable.berg.cli.assemble
  "Implementation of the command line utility for assembling the site from source files and a template."
  (:require [clojure.java.io :as io]
            [danhable.berg.site :as site]
            [danhable.berg.cli.common :refer [exec> site-options]])
  (:import [org.apache.commons.io FileUtils]))


(defn -main [& _]
  (exec> "Cleaning prior assembled site files..." (FileUtils/deleteDirectory (io/as-file (:target site-options))))
  (exec> "Assembling site from source..." (site/assemble-site site-options)))

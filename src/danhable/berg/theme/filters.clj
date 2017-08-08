(ns danhable.berg.theme.filters
  "Contains functions designed to be loaded into selmer and exposed as
  filters that can be used in the templates. Filters here were added to
  make the process of writing templates easier and with less code even if
  the same functionality could be achieved using built in filters."
  (:require [selmer.filters :as selmer-filters])
  (:import [java.text SimpleDateFormat]))


(defn group-by-year-filter
  "Groups coll of maps or records by the posted year (using the key selector [:data :posted]
   to access the posted value). Returns a map of with the year as the key and a sequence of
   pages as the result."
  [coll]
  (let [ks [:data :posted]]
    (->> coll
         (filter #(not (nil? (get-in % ks))))
         (group-by #(.format (SimpleDateFormat. "yyyy")
                             (get-in % ks))))))


(defn load-custom-filters!
  "Exposes the custom filters in selmer using the add-filters! function. This has a side
  effect of mutating the selmer global state because the selmer API requires it. Returns
  nil."
  []
  (selmer-filters/add-filter! :group-by-year group-by-year-filter))


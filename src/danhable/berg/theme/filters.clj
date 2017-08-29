(ns danhable.berg.theme.filters
  "Contains functions designed to be loaded into selmer and exposed as
  filters that can be used in the templates. Filters here were added to
  make the process of writing templates easier and with less code even if
  the same functionality could be achieved using built in filters."
  (:require [clojure.string :as string]
            [clj-time.core :as clj-time]
            [selmer.filters :as selmer-filters]))


(defn group-by-year-filter
  "Groups coll of maps or records by the posted year (using the key selector [:data :posted]
   to access the posted value). Returns a map of with the year as the key and a sequence of
   pages as the result."
  [coll]
  (let [ks [:data :posted]]
    (->> coll
         (filter #(not (nil? (get-in % ks))))
         (group-by #(clj-time/year (get-in % ks))))))


(defn nested-sort-by-filter
  "Sorts coll but allows for for specifying a key value that is nested
  multiple levels in (separated by a dot between levels). Returns a new collection
  with coll elements sorted by the nested key."
  [coll nested-key]
  (let [keys (->> (string/split nested-key #"\.")
                  (map keyword)
                  vec)]
    (sort-by #(get-in % keys) coll)))


(defn nested-sort-by-reversed-filter
  "Just like nested-sort-by-filter but in descending order."
  [coll nested-key]
  (let [keys (->> (string/split nested-key #"\.")
                  (map keyword)
                  vec)]
    (sort-by #(get-in % keys) (comp - compare) coll)))


(defn load-custom-filters!
  "Exposes the custom filters in selmer using the add-filters! function. This has a side
  effect of mutating the selmer global state because the selmer API requires it. Returns
  nil."
  []
  (selmer-filters/add-filter! :group-by-year group-by-year-filter)
  (selmer.filters/add-filter! :nested-sort-by nested-sort-by-filter)
  (selmer.filters/add-filter! :nested-sort-by-reversed nested-sort-by-reversed-filter))


(ns danhable.berg.site
  "Contains the logic and data structures that define what the entire site looks like.
  A site contains a set of configuration items, a sequence of Page objects and a Theme
  object. Some elements, like tags, are computed from the Page data. Having them hardcoded
  in the site seems like it could be improved later on, just not sure how I want to abstract
  that logic out at this point."
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [danhable.berg.page :as page]
            [danhable.berg.theme :as theme]))


(defrecord Site [pages theme conf target-dir tags])


(def ^:private default-site-conf {:title "Caffeinated Ideas"
                                  :description ""
                                  :author "Dan Hable"
                                  :site-url "http://caffeinatedideas.com"
                                  :date-format "YYYY MMM dd"
                                  :theme "resources/themes/default"
                                  :sources "resources/site"
                                  :target "target/site"})


(defn update-all
  "For every element in ks, updates the value for that key in m by applying
  function f to the current value. Unlike update-in, this function does not
  support nested maps. It is designed for multiple, similar updates to a single
  map level."
  [m ks f]
  (if (and f m)
    (reduce #(update-in %1 [%2] f)
            m
            ks)
    m))


(defn build-tag-set
  "Given a sequence of pages, returns a set of all the tags contained in the page
  data. Returns an empty set if pages is nil, empty or no :tags element is present
  in the page data."
  [pages]
  (transduce (comp
               (map #(get-in % [:data :tags]))
               (map set))
             set/union
             #{}
             pages))


(defn make-site-context
  [site]
  (merge (select-keys site [:tags :pages])
         (select-keys (:conf site) [:title :description :author :date-format :site-url])))


(defn new-Site
  [options]
  (let [site-conf (as-> options $
                        (merge default-site-conf $)
                        (update-all $ [:theme :sources :target] io/as-file)
                        (select-keys $ (keys default-site-conf)))
        pages (page/load-all-pages (:sources site-conf))]
    (map->Site {:conf site-conf
                :target-dir (:target site-conf)
                :pages pages
                :theme (theme/new-Theme (:theme site-conf))
                :tags (build-tag-set pages)})))


(defn compile-site
  [site]
  (let [{:keys [pages theme]} site
        post-rendered-pages (map #(let [context {:site (make-site-context site)
                                                 :data (page/make-page-context %1)}
                                        rendered-page (theme/apply-to-page theme %1 context)]
                                    (assoc %1 :rendered-view rendered-page)) pages)]
    (assoc site :pages post-rendered-pages)))


(defn write-to-disk
  [site]
  (let [target-dir (:target-dir site)]
    (theme/copy-static-files (:theme site) target-dir)
    (doseq [p (:pages site)]
      (page/write-rendered-page p target-dir))))

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


(defn template-context
  [site page]
  {:site (merge (select-keys site [:tags :pages])
                (select-keys (:conf site) [:title :description :author :date-format :site-url]))
   :data (get page :data)})


(defn new-Site
  [{:keys [sources target theme] :as options}]
  (let [pages (page/load-all-pages (io/file sources))]
    (map->Site {:conf options
                :target-dir (io/file target)
                :pages pages
                :theme (theme/new-Theme (io/file theme))
                :tags (build-tag-set pages)})))


(defn generate-site-pages
  [site]
  (let [{:keys [pages theme]} site
        post-rendered-pages (map #(let [context (template-context site %1)
                                        rendered-page (theme/apply-to-page theme %1 context)]
                                    (assoc %1 :rendered-view rendered-page)) pages)]
    (assoc site :pages post-rendered-pages)))


(defn write-to-disk
  [site]
  (let [target-dir (:target-dir site)]
    (theme/copy-static-files (:theme site) target-dir)
    (doseq [p (:pages site)]
      (page/write-rendered-page p target-dir))))


(defn assemble-site
  [site-options]
  (-> (new-Site site-options)
      generate-site-pages
      write-to-disk))

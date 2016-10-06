(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.9.0-alpha10"]
                    [markdown-clj "0.9.89"]
                    [selmer "1.0.7"]
                    [clj-time "0.12.0"]])

(require '[danhable.site-gen :refer :all])

(task-options!
  site-gen {:post-dir     "./resources/posts"
            :template-dir "./resources/template"
            :site-dir     "./target/site"})


;; Tasks:
;;
;;  test - Run unit tests on clojure code
;;  site-gen - Compile source into static site
;;  publish - Sync site to S3 or other server
;;
;; V2:
;;  spell-check - Run spell checker on my posts
;;  dead-link-check - Check all links on the site to see if still valid
;;

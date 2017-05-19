(set-env!
  :source-paths   #{"src/clojure" "tests" "src/site"}
  :resource-paths #{"resources"}
  :dependencies   '[[org.clojure/clojure "1.9.0-alpha16"]
                    [markdown-clj "0.9.99"]
                    [selmer "1.10.7"]
                    [clj-time "0.13.0"]
                    [boot/core "RELEASE" :scope "test"]
                    [adzerk/boot-test "1.2.0" :scope "test"]
                    [onetom/boot-lein-generate "0.1.3" :scope "test"]])

(require '[adzerk.boot-test :refer :all]
         '[boot.lein]
         '[danhable.berg.boot :refer :all])

(boot.lein/generate)


;;(task-options!
;;  generate-site {:post-dir     "resources/posts"
;;                 :page-dir     "resources/pages"
;;                 :template-dir "resources/template"
;;                 :site-dir     "target/site"})


;; Tasks:
;;
;;  test - Run unit tests on clojure code
;;  generate-site - Compile source into static site
;;  publish - Sync site to S3 or other server
;;
;; V2:
;;  spell-check - Run spell checker on my posts
;;  dead-link-check - Check all links on the site to see if still valid
;;

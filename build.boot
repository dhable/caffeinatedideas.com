(set-env!
  :source-paths #{"src" "tests"}
  :resource-paths #{"resources" "dev-resources"}
  :dependencies   '[[org.clojure/clojure "1.8.0"]
                    [markdown-clj "0.9.99"]
                    [selmer "1.10.7"]
                    [clj-time "0.13.0"]
                    [boot/core "RELEASE" :scope "test"]
                    [adzerk/boot-test "1.2.0" :scope "test"]
                    [tolitius/boot-check "0.1.4"]
                    [onetom/boot-lein-generate "0.1.3" :scope "test"]
                    [hashobject/boot-s3 "0.1.2-SNAPSHOT"]])

(require '[adzerk.boot-test :refer :all]
         '[tolitius.boot-check :as check]
         '[boot.lein]
         '[danhable.berg.boot :refer :all]
         '[danhable.berg.io :refer [load-properties]]
         '[hashobject.boot-s3 :refer :all])

(boot.lein/generate)

(def s3-properties (load-properties "s3.properties"))

(task-options!
  assemble {:source "resources/site"
            :theme  "resources/themes/default"
            :output "target"}
  s3-sync {:source "."
           :bucket (.get s3-properties "bucket")
           :access-key (.get s3-properties "access-key")
           :secret-key (.get s3-properties "secret-key")})

(deftask check-source-code
  "Check Clojure source code for problems with kibit."
  []
  (set-env! :source-paths #{"src" "tests"})
  (comp
    (check/with-kibit)))

(deftask publish
  "Build the site from sources and then upload the result to S3."
  []
  (comp
    (assemble)
    (s3-sync)))


;; V2:
;;  spell-check - Run spell checker on my posts
;;  dead-link-check - Check all links on the site to see if still valid
;;
;; V3:
;;  cloudfront cache invalidation and support
;;  prevent regenerating pages if they didn't change

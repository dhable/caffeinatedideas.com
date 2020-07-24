(ns danhable.berg.page-test
  (:require [clojure.test :refer :all]
            [schema.test :as schema-test]
            [danhable.berg.page :refer :all]
            [clojure.java.io :as io]))

(use-fixtures :once schema-test/validate-schemas)


(deftest test-is-page-source
  (testing "should return false when path is nil"
    (is (false? (is-page-source? nil))))

  (testing "should return false when path filename does not contain an extension"
    (let [test-path (.toPath (io/as-file "lock"))]
      (is (false? (is-page-source? test-path)))))

  (testing "should return false when path filename is not an EDN extension"
    (let [test-path (.toPath (io/as-file "lock.txt"))]
      (is (false? (is-page-source? test-path)))))

  (testing "should return false when path filename is a dot file"
    (let [test-path (.toPath (io/as-file ".keep"))]
      (is (false? (is-page-source? test-path)))))

  (testing "should return true when path filename is an EDN file"
    (let [test-path (.toPath (io/as-file "resume.edn"))]
      (is (true? (is-page-source? test-path))))))


(deftest test-read-page-content
  (testing "should return Clojure data structure resolving all reader macros in process"
    (is (= {:template-name "post"
            :resources []
            :data {:title "This is a title"
                   :content "This is file b.txt"}}
           (read-page-content (io/as-file "dev-resources/page_tests/included_content.edn"))))))


(deftest test-new-Page
  (testing "create new page object from source file"
    (let [page (new-Page (io/as-file "dev-resources/page_tests") (io/as-file "dev-resources/page_tests/included_content.edn"))]
      (is (= "dev-resources/page_tests/included_content.edn" (str (get page :source-file))))
      (is (= "included_content.html" (str (get page :url-path))))
      (is (= "post" (get page :template-name)))
      (is (= [] (get page :resources)))
      (is (=  {:title "This is a title", :content "This is file b.txt"}
              (:data page))))))

(deftest test-load-all-pages
  (testing "loading all page objects"
    (let [pages (load-all-pages (io/as-file "dev-resources/page_tests"))
          source-files (map #(str (get % :source-file)) pages)]
      (is (= ["dev-resources/page_tests/included_content.edn"
              "dev-resources/page_tests/subdir/other.edn"]
             source-files)))))

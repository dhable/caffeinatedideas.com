(ns danhable.berg.page-tests
  (:require [clojure.test :refer :all]
            [danhable.berg.page :refer :all]
            [clojure.java.io :as io]))


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


(deftest test-path-bound-slurp
  (testing "should return function bound to a directory when asking for file content"
    (let [test-base-dir (io/as-file "resources/page_tests")
          bound-slurp (path-bound-slurp test-base-dir)]
      (is (= "This is file b.txt"
             (bound-slurp "b.txt"))))))


(deftest test-read-page-content
  (testing "should return Clojure data structure resolving all reader macros in process"
    (is (= {:title "This is a title"
            :resources {}
            :template-name "post"
            :content "This is file b.txt"}
           (read-page-content (io/as-file "resources/page_tests/included_content.edn"))))))


(deftest test-new-Page
  (testing "create new page object from source file"
    (let [page (new-Page (io/as-file "resources/page_tests") (io/as-file "resources/page_tests/included_content.edn"))]
      (is (= "resources/page_tests/included_content.edn" (.toString (:source-file page))))
      (is (= "included_content.html" (.toString (:url-path page))))
      (is (= "post" (:template-name page)))
      (is (= {} (:resources page)))
      (is (=  {:title "This is a title", :content "This is file b.txt"}
              (:data page))))))

(deftest test-load-all-pages
  (testing "loading all page objects"
    (let [pages (load-all-pages (io/as-file "resources/page_tests"))
          source-files (map #(.toString (:source-file %)) pages)]
      (is (= ["resources/page_tests/included_content.edn"
              "resources/page_tests/subdir/other.edn"]
             source-files)))))

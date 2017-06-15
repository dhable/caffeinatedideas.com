(ns danhable.berg.site-tests
  (:require [clojure.test :refer :all]
            [danhable.berg.site :refer :all]
            [danhable.berg.page :as page]
            [clojure.java.io :as io]))


(deftest test-update-all
  (let [test-input {"abc" 1 "bcd" 2 "cde" 3 "def" 4}]
    (testing "should return the source map unmodified when the list of keys is an empty list"
        (is (= test-input (update-all test-input [] inc))))

    (testing "should return the source map unmodified when the function to apply is nil"
      (is (= test-input (update-all test-input ["abc"] nil))))

    (testing "should return nil when the source map is nil"
      (is (nil? (update-all nil ["abc"] inc))))

    (testing "should apply function to single key in the map when only one element is supplied"
      (is (= {"abc" 1 "bcd" 3 "cde" 3 "def" 4}
             (update-all test-input ["bcd"] inc))))

    (testing "should apply function to multiple keys in the map when they match"
      (is (= {"abc" 2 "bcd" 3 "cde" 4 "def" 5}
             (update-all test-input ["bcd" "def" "abc" "cde"] inc))))))


(deftest test-build-tag-set
  (testing "should return empty set when pages is nil"
    (is (= #{} (build-tag-set nil))))

  (testing "should return empty set when pages is an empty sequence"
    (is (= #{} (build-tag-set []))))

  (testing "should return empty set if page data doesn't contain tags vector"
    (let [test-pages [(page/->Page (io/as-file "dev-resources/page_tests/included_content.edn") "included_content.html" "post" [] {} nil)
                      (page/->Page (io/as-file "dev-resources/page_tests/subdir/other.edn") "subdir/other.html" "post" [] {} nil)]]
      (is (= #{} (build-tag-set test-pages)))))

  (testing "should return empty set if page data contains empty tag vector"
    (let [test-pages [(page/->Page (io/as-file "dev-resources/page_tests/included_content.edn") "included_content.html" "post" [] {:tags []} nil)
                      (page/->Page (io/as-file "dev-resources/page_tests/subdir/other.edn") "subdir/other.html" "post" [] {:tags []} nil)]]
      (is (= #{} (build-tag-set test-pages)))))

  (testing "should return  set of tags from the tags vector on all pages"
    (let [test-pages [(page/->Page (io/as-file "dev-resources/page_tests/included_content.edn") "included_content.html" "post" [] {:tags ["a" "b" "c"]} nil)
                      (page/->Page (io/as-file "dev-resources/page_tests/subdir/other.edn") "subdir/other.html" "post" [] {:tags ["c" "d"]} nil)]]
      (is (= #{"a" "b" "c" "d"} (build-tag-set test-pages))))))

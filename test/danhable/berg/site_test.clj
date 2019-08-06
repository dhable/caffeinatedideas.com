(ns danhable.berg.site-test
  (:require [clojure.test :refer :all]
            [danhable.berg.site :refer :all]
            [danhable.berg.page :as page]
            [clojure.java.io :as io]))


#_(deftest test-build-tag-set
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

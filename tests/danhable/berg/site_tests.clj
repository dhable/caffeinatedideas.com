(ns danhable.berg.site-tests
  (:require [clojure.test :refer :all]
            [danhable.berg.site :refer :all]))


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

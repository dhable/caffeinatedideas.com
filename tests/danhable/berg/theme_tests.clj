(ns danhable.berg.theme-tests
  (:require [clojure.test :refer :all]
            [danhable.berg.theme :refer :all]
            [clojure.java.io :as io])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))


(deftest test-is-private-template-file?
  (testing "should return false if path is nil"
    (is (false? (is-private-template-file? nil))))

  (testing "should return true if filename starts with an underscore"
    (is (-> "_base.html"
            io/file
            .toPath
            is-private-template-file?)))

  (testing "should return true if path contains dir info and starts with an underscore"
    (is (-> "theme/2016/templates/_base.html"
            io/file
            .toPath
            is-private-template-file?)))

  (testing "should return false if filename does not start with underscore"
    (is (not-every? #(-> %
                         io/file
                         .toPath
                         is-private-template-file?)
                    ["base.html" "b_ase.html" "1base.html" "+base.html"]))))


(deftest test-template-name
  (testing "should return template name given a File object"
    (is (= "archive" (template-name (io/as-file "archive.html")))))

  (testing "should return template name correctly if there is no extension"
    (is (= "archive" (template-name (io/as-file "archive")))))

  (testing "should return nil if the File object is nil"
    (is (nil? (template-name nil)))))


(deftest test-load-templates
  (testing "should return nil if base-dir does not exist"
    (let [actual (load-templates (io/as-file "dev-resources/theme_tests/doesnotexist"))]
      (is (nil? actual))))

  (testing "should return empty map if no templates exist"
    (let [new-empty-dir (Files/createTempDirectory "theme-tests" (make-array FileAttribute 0))
          actual (load-templates (.toFile new-empty-dir))]
      (is (= {} actual))))

  (testing "should load up templates"
    (let [actual (load-templates (io/as-file "dev-resources/theme_tests/templates"))]
      (is (= ["index" "thing"] (keys actual))))))


(deftest test-load-static-files
  (testing "should return nil if base-dir does not exist"
    (let [actual (load-static-files (io/as-file "dev-resources/theme_tests/doesnotexist"))]
      (is (nil? actual))))

  (testing "should return empty map if no static files exist"
    (let [new-empty-dir (Files/createTempDirectory "theme-tests" (make-array FileAttribute 0))
          actual (load-static-files (.toFile new-empty-dir))]
      (is (= {} actual))))

  (testing "should return static files"
    (let [actual (load-static-files (io/as-file "dev-resources/theme_tests/static"))]
      (is (= ["app.css" "images/a.jpg"]
             (keys actual))))))


(deftest test-apply-to-page
  (testing "should render a template"
    (let [test-theme (new-Theme (io/as-file "dev-resources/theme_tests"))]
      (is (= "<h1>Base Template</h1>\n\n<h3>Thing SUB TEXT</h3>\n"
             (apply-to-page test-theme
                            {:template-name "thing"
                             :data {:thing "SUB TEXT"}}
                            {:site nil :page {:thing "SUB TEXT"}}))))))


(deftest test-new-Theme
  (testing "should find all static files"
    (let [test-theme (new-Theme (io/as-file "dev-resources/theme_tests"))]
      (is (= ["app.css" "images/a.jpg"]
             (-> test-theme
                 (get :static-files)
                 keys))))))

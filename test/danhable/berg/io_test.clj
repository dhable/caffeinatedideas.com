(ns danhable.berg.io-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [schema.test :as schema-test]
            [danhable.berg.io :refer :all]))

(use-fixtures :once schema-test/validate-schemas)


(deftest test-normalize-extension
  (testing "should return nil when extension is nil"
    (is (nil? (normalize-extension nil))))

  (testing "should return empty string when extension is an empty string"
    (is (= "" (normalize-extension ""))))

  (testing "should return trim whitespace and return starting with a dot when extension does not"
    (is (= ".html" (normalize-extension "html")))
    (is (= ".tar.gz" (normalize-extension "tar.gz")))
    (is (= ".html" (normalize-extension " html")))
    (is (= ".html" (normalize-extension "html "))))

  (testing "should return trim whitespace and as is when extension leads with a dot"
    (is (= ".html" (normalize-extension ".html")))
    (is (= ".tar.gz" (normalize-extension ".tar.gz")))
    (is (= ".html" (normalize-extension " .html")))
    (is (= ".html" (normalize-extension ".html ")))))


(deftest test-trim-extension
  (testing "should return nil when filename is nil"
    (is (nil? (trim-extension nil))))

  (testing "should not trim filenames that do not have extensions"
    (is (= "" (trim-extension "")))
    (is (= " " (trim-extension " ")))
    (is (= "metadata" (trim-extension "metadata")))
    (is (= " metadata" (trim-extension " metadata")))
    (is (=  " metadata " (trim-extension " metadata "))))

  (testing "should trim extensions from filenames"
    (is (= "metadata" (trim-extension "metadata.edn")))
    (is (= "metadata" (trim-extension "metadata.edn ")))
    (is (= " metadata" (trim-extension " metadata.edn")))
    (is (= " metadata" (trim-extension " metadata.edn ")))))


(deftest test-get-file-extension
  (testing "should return nil when filename is nil"
    (is (nil? (get-file-extension nil))))

  (testing "should return empty string if there is no extension"
    (is (= "" (get-file-extension "metadata"))))

  (testing "should return extension if present"
    (is (= ".edn" (get-file-extension "metadata.edn")))))


(deftest test-replace-file-extension
  (let [test-file (io/file "/tmp/blog/metadata.edn")
        test-file-without-ext (io/file "/tmp/blog/metadata")]
    (testing "should return original file unchanged if new-extension is nil"
      (is (= "/tmp/blog/metadata.edn"
             (str (replace-file-extension test-file nil)))))

    (testing "should return original without extension if new-extension is emptry string"
      (is (= "/tmp/blog/metadata"
             (str (replace-file-extension test-file "")))))

    (testing "should replace the extension if new-extension starts with a dot"
      (is (= "/tmp/blog/metadata.html"
             (str (replace-file-extension test-file ".html")))))

    (testing "should add dot if the new-extension does not start with a dot"
      (is (= "/tmp/blog/metadata.html"
             (str (replace-file-extension test-file "html")))))

    (testing "should append new-extension if original file does not have an extension"
      (is (= "/tmp/blog/metadata.html"
             (str (replace-file-extension test-file-without-ext "html")))))))


(deftest test-list-files
  (let [resource-dir (io/as-file "dev-resources/list_files_test")]
    (testing "should list all files when no options supplied"
      (is (= (map str (list-files resource-dir))
            ["dev-resources/list_files_test/a.edn"
             "dev-resources/list_files_test/b.edn"
             "dev-resources/list_files_test/c.txt"])))

    (testing "should list all files in all subdirectories as well when recursive is used"
      (is (= (map str (list-files resource-dir :recursive? true))
             ["dev-resources/list_files_test/a.edn"
              "dev-resources/list_files_test/b.edn"
              "dev-resources/list_files_test/c.txt"
              "dev-resources/list_files_test/sub1/d.edn"
              "dev-resources/list_files_test/sub1/sub2/e.edn"])))

    (testing "should only return files when filter returns true for them"
      (is (= (map str (list-files resource-dir :recursive? true :filter #(.. % getFileName toString (endsWith ".edn"))))
             ["dev-resources/list_files_test/a.edn"
              "dev-resources/list_files_test/b.edn"
              "dev-resources/list_files_test/sub1/d.edn"
              "dev-resources/list_files_test/sub1/sub2/e.edn"])))

    (testing "should return an empty list if the filter matches nothing"
      (is (empty? (list-files resource-dir :recursive? true :filter (constantly false)))))))


(deftest test-touch
  (testing "should create file that doesn't exist yet"
    (let [tmpdir (create-tmp-dir! "unit")
          test-file (io/file tmpdir "testfile.txt")]
      (is (not (.exists test-file)))
      (touch! test-file)
      (is (.exists test-file))))

  (testing "should create necessary directories when creating new file"
    (let [tmpdir (create-tmp-dir! "unit")
          test-file (io/file tmpdir "a/b/c/d.txt")]
      (is (not (.exists test-file)))
      (touch! test-file)
      (is (.exists test-file)))))


(deftest test-as-path
  (testing "should return Path object as self"
    (let [p (.toPath (io/as-file "/tmp/a.txt"))]
      (is (identical? p (as-path p)))))

  (testing "should return Path object if provided with a File instance"
    (let [f (io/as-file "/tmp/b.txt")
          p (as-path f)]
      (is (= (str f)
             (str p)))))

  (testing "should return Path object if provided with a String instance"
    (is (= "/tmp/c.txt"
           (str (as-path "/tmp/c.txt"))))))


(deftest test-relativize
  (testing "should return path and file portion not specific to base path"
    (let [actual (relativize "/a/b" "/a/b/c/d.txt")]
      (is (= "c/d.txt"
             (str actual)))))

  (testing "should go up directory level base and file are in different paths"
    (let [actual (relativize "/x/y/z" "/a/b/c/d.txt")]
      (is (= "../../../a/b/c/d.txt"
             (str actual)))))

  (testing "should only include the filename if the base path and file path are same"
    (let [actual (relativize "/a/b/c" "/a/b/c/d.txt")]
      (is (= "d.txt"
             (str actual))))))


(deftest test-load-properties
  (testing "should return nil if file doesn't exist"
    (is (nil? (load-properties "dev-resources/load_properties_test/missing.properties"))))

  (testing "should load valid properties file"
    (let [actual (load-properties "dev-resources/load_properties_test/test.properties")]
      (is (= "1" (.getProperty actual "a")))
      (is (= "2" (.getProperty actual "b")))
      (is (= "3" (.getProperty actual "c"))))))

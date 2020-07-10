(ns danhable.berg.io
  "Enhanced I/O functions that extend and build upon the clojure.java.io namespace,
  java.io package and the java.nio.files package."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io PushbackReader File]
           [java.util EnumSet Properties]
           [java.nio.file Files FileVisitOption FileVisitResult SimpleFileVisitor Path]
           [java.nio.file.attribute FileAttribute]))


(defn pushback-reader
      "Creates an instance of java.io.PushbackReader given a file x and various
      opts. These are the same as clojure.java.io/reader."
      [x & opts]
      (PushbackReader. (apply io/reader x opts)))


(defn normalize-extension
  "Takes some text intended to be used as an extension for a filename and
  normalizes to ensure that it starts with a dot. If the extension text
  already starts with a dot, is of 0 length or nil then that value is
  returned unaltered."
  [^String extension]
  (when-not (nil? extension)
    (let [te (str/trim extension)]
      (cond
        (zero? (.length te))      te
        (str/starts-with? te ".") te
        :else                     (str "." (str/trim te))))))


(defn trim-extension
  "Takes a filename string and returns the filename without the extension or the
  dot separator. If the filename is nil or doesn't contain an extension, the value
  is simply returned."
  [^String filename]
  (when filename
    (if-let [ext-pos (str/last-index-of filename ".")]
      (subs filename 0 ext-pos)
      filename)))


(defn get-file-extension
  "Takes a filename string and returns the extension, which is defined to be the
  part of the filename after the right most dot character. If the filename is nil,
  the value returned is nil. In cases where there is no extension, an empty string
  is returned."
  [^String filename]
  (when filename
    (if-let [ext-pos (str/last-index-of filename ".")]
      (subs filename ext-pos)
      "")))


(defn replace-file-extension
  "Replaces any existing extension on java.io.File f with new-extension and return
  the results as a new java.io.File. If f is missing an extension, new-extension is
  appended. If new-extension is nil, f is returned unchanged."
  ^File [^File f new-extension]
  (if (nil? new-extension)
    f
    (io/file (.getParent f) (-> f
                                .getName
                                trim-extension
                                (str (normalize-extension new-extension))))))


(defn list-files
  "Walk the filesystem starting at cwd and generate a list of java.io.File objects for all
  located files. Optionally takes two key/value pairs with additional options:

    recursive? - a boolean that can limit the file list to a single path or to recursively
                 generate a list of files

    filter - a function that will be called with each Path object and returns true if the
             path should be included in the result."
  [^File cwd & {:keys [recursive? filter]}]
  (let [results (atom [])
        filter-fn (or filter
                      (constantly true))]
    (Files/walkFileTree (.toPath cwd)
                        (EnumSet/noneOf FileVisitOption)
                        (if recursive? Integer/MAX_VALUE 1)
                        (proxy [SimpleFileVisitor] []
                          (visitFile [path _]
                            (when (and (.. path toFile isFile)
                                       (filter-fn path))
                              (swap! results conj (.toFile path)))
                            FileVisitResult/CONTINUE)))
    (sort @results)))


(defn create-tmp-dir!
  "Wrapper around Java NIO Files implementation of createTempDirectory that defaults the
  file attributes and also sets the directory to delete on exit when finishing up. Returns
  a java.io.File instance to a new temp directory."
  [prefix]
  (let [default-attr (make-array FileAttribute 0)
        tmp-dir-path (Files/createTempDirectory prefix default-attr)
        tmp-dir-file (.toFile tmp-dir-path)]
    (.deleteOnExit tmp-dir-file)
    tmp-dir-file))


(defn touch!
  "Creates file f and all parent directories if they don't already exist. Returns nil."
  [f]
  (.mkdirs (.getParentFile f))
  (.createNewFile f))


(defn as-path
  "Attempt to corearse x into a java.nio.files.Path object instance."
  [x]
  (if (instance? Path x)
    x
    (.toPath (io/as-file x))))


(defn relativize
  "A nicer wrapper around the Path relativize method that accepts instances of
  Strings, Files or Paths as input."
  [base f]
  (.relativize (as-path base) (as-path f)))


(defn load-properties
  "Wrapper around creating a java.util.Properties instance with the contents from
  filename. Returns nil if filename does not reference an actual file."
  [filename]
  (when (.exists (io/file filename))
    (with-open [stream (io/input-stream filename)]
      (doto (Properties.) (.load stream)))))

(defn content-type
  "Wrapper around Java 7 NIO method for trying to determine the MIME content type of a file."
  [f]
  (Files/probeContentType (as-path f)))

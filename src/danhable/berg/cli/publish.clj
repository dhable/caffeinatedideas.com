(ns danhable.berg.cli.publish
  "Implementation of the command line utility for syncing the assembled files to the destination S3 bucket."
  (:require [clojure.java.io :as io]
            [cognitect.aws.client.api :as aws]
            [danhable.berg.io :as io+]
            [danhable.berg.cli.common :refer [exec> site-options]]
            [danhable.berg.cli.assemble :as assemble]))


(defn fetch-objects
  "Returns a lazy sequence of object keys from an S3 bucket using the S3 paginated API."
  ([s3 bucket keys] (fetch-objects s3 bucket keys {:Contents [] :IsTruncated true}))
  ([s3 bucket keys prior-state]
   (lazy-seq
     (let [{old-contents :Contents old-truncated? :IsTruncated token :NextContinuationToken} prior-state
           curr-state (if (and old-truncated? (empty? old-contents))
                        (aws/invoke s3 {:op :ListObjectsV2 :request {:Bucket bucket :ContinuationToken token}})
                        prior-state)
           elem (first (:Contents curr-state))
           next-state (update curr-state :Contents rest)]
       (when elem
         (cons (select-keys elem keys)
               (fetch-objects s3 bucket keys next-state)))))))


(defn determine-transfer-plan
  "Compares all files found from base-dir to all the S3 objects to determine which keys
  in S3 need to be uploaded again or deleted in order to make the S3 bucket a representation
  of base-dir. Returns a hashmap with S3 key names as the key and values of either a java.io.File
  or the keyword :delete if the S3 key should be removed."
  [base-dir objects]
  (let [plan      (->> (io+/list-files (io/as-file base-dir) :recursive? true)
                       (reduce #(assoc %1 (str (io+/relativize base-dir %2)) %2) {}))
        reduce-fn (fn reduce-fn
                    [acc {:keys [Key LastModified]}]
                    (cond
                      (not (contains? acc Key))
                        (assoc acc Key :delete)
                      (<= (.lastModified (get acc Key)) (.getTime LastModified))
                        (dissoc acc Key)
                      :else
                        acc))]
    (->> objects
         (filter #(< 0 (:Size %)))
         (filter #(not (contains? (:s3-whitelist site-options) (:Key %))))
         (reduce reduce-fn plan))))


(defn transfer-files
  "Given a s3 client, a bucket name and the plan hashmap from determine-transfer-plan, execute
  PUT and DELETE operations on the S3 bucket to reflect the plan."
  [s3 bucket plan]
  (doseq [[key value] plan]
    (if (= value :delete)
      (aws/invoke s3 {:op :DeleteObject :request {:Bucket bucket :Key key}})
      (aws/invoke s3 {:op :PutObject :request {:Bucket bucket
                                               :Key key
                                               :ContentLength (.length value)
                                               :ContentType (io+/content-type value)
                                               :Body (io/input-stream value)}}))))


(defn -main [& args]
  (assemble/-main args)
  (let [args-map (apply hash-map args)
        bucket   (get args-map "--bucket" (:bucket site-options))
        s3       (aws/client {:api :s3})]
    (as->
      (exec> "Fetching existing S3 bucket content..." (fetch-objects s3 bucket [:Key :LastModified :Size])) $
      (exec> "Building list of files that need updating..." (determine-transfer-plan (:target site-options) $))
      (exec> "Transferring new/update files to S3..." (transfer-files s3 bucket $)))))

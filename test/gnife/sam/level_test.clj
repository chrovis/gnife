(ns gnife.sam.level-test
  (:require [cljam.io.sam :as sam]
            [clojure.java.io :as jio]
            [clojure.test :refer [deftest is]]
            [gnife.sam.level :as level]
            [gnife.test :refer [with-tmp-dir]]
            [gnife.test.resource :as resource]))

(def ^:private test-sorted-bam-levels
  [{:type "i", :value 0}
   {:type "i", :value 1}
   {:type "i", :value 2}
   {:type "i", :value 2}
   {:type "i", :value 0}
   {:type "i", :value 0}
   {:type "i", :value 0}
   {:type "i", :value 1}
   {:type "i", :value 2}
   {:type "i", :value 3}
   {:type "i", :value 4}
   {:type "i", :value 5}])

(deftest level-test
  (with-tmp-dir tmp-dir
    (let [tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
      (is (zero? (:status (level/level [resource/test-sorted-bam tmp-bam]))))
      (with-open [rdr (sam/bam-reader tmp-bam)]
        (is (= (map #(first (keep :LV (:options %))) (sam/read-alignments rdr))
               test-sorted-bam-levels)))

      (is (thrown? clojure.lang.ExceptionInfo
                   (level/level [resource/test-bam tmp-bam]))))))

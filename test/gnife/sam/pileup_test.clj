(ns gnife.sam.pileup-test
  (:require [clojure.java.io :as jio]
            [clojure.test :refer [are deftest]]
            [gnife.sam.pileup :as pileup]
            [gnife.test.resource :as resource]))

(deftest pileup-test
  ;; NB: "pileup" output format may change in future (maybe)
  (doseq [t [1 4]]
    (are [options file] (= (with-out-str
                             (pileup/pileup [resource/test-sorted-bam] options))
                           (slurp (jio/file "resources/sam/pileup" file)))
      {:thread t, :simple true} "s.pileup"
      {:thread t, :ref resource/test-fasta} "f.pileup"
      {:thread t, :simple true, :ref resource/test-fasta} "sf.pileup"
      {:thread t, :region "ref2"} "r1.pileup"
      {:thread t, :region "ref2", :simple true} "r1s.pileup"
      {:thread t, :region "ref2", :ref resource/test-fasta} "r1f.pileup"
      {:thread t, :region "ref2", :simple true, :ref resource/test-fasta} "r1sf.pileup"
      {:thread t, :region "ref2:10-200"} "r2.pileup"
      {:thread t, :region "ref2:10-200", :simple true} "r2s.pileup"
      {:thread t, :region "ref2:10-200", :ref resource/test-fasta} "r2f.pileup"
      {:thread t, :region "ref2:10-200", :simple true, :ref resource/test-fasta} "r2sf.pileup")))

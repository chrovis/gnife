(ns gnife.main-test
  (:require [clojure.java.io :as jio]
            [clojure.string :as string]
            [clojure.test :refer [are deftest is testing use-fixtures]]
            [gnife.main :as main]
            [gnife.test :refer [disable-log-fixture same-sam-contents?
                                same-sequence-contents? slurp-sam slurp-bam
                                slurp-vcf with-out-noop with-tmp-dir]]
            [gnife.test.resource :as resource :refer :all]
            [gnife.util :as util]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m])
  (:import java.io.StringWriter))

(use-fixtures :once disable-log-fixture)

(defn- test-call
  [f]
  (let [w (StringWriter.)]
    (binding [*out* w]
      (let [ret (f)]
        {:ret ret
         :out (str w)}))))

(deftest tree-test
  (is (match? (m/regex #"^(\n*[0-9a-z]+(\n  [0-9a-z]+.+)+)+$")
              (main/tree))))

(deftest sequence-faidx-test
  (with-tmp-dir tmp-dir
    (let [tmp-fasta (.getAbsolutePath (jio/file tmp-dir "tmp.fa"))]
      (jio/copy (jio/file test-fasta) (jio/file tmp-fasta))
      (is (match? {:ret {:status zero?}}
                  (test-call #(main/exec :sequence ["faidx" tmp-fasta]))))
      (is (.exists (jio/file (str tmp-fasta ".fai")))))))

(deftest sequence-dict-test
  (with-tmp-dir tmp-dir
    (let [tmp-fasta (.getAbsolutePath (jio/file tmp-dir "tmp.fa"))]
      (jio/copy (jio/file test-fasta) (jio/file tmp-fasta))
      (is (match? {:ret {:status zero?}}
                  (test-call #(main/exec :sequence ["dict" tmp-fasta]))))
      (is (.exists (jio/file (str (util/corepath tmp-fasta) ".dict")))))))

(deftest sam-view-test
  (are [args] (zero? (with-out-noop
                       (:status (main/exec :sam args))))
    ["view" test-sam]
    ["view" "-f" "sam" test-sam]
    ["view" test-bam]
    ["view" "-f" "bam" test-bam]
    ["view" "--header" test-bam]
    ["view" "-r" "ref2" test-sorted-bam]
    ["view" "-r" "ref2:10-200" test-sorted-bam]))

(deftest sam-convert-test
  (with-tmp-dir tmp-dir
    (testing "SAM -> BAM"
      (let [tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
        (is (zero? (:status (main/exec :sam ["convert" test-sam tmp-bam]))))
        (is (= (slurp-bam tmp-bam) (slurp-sam test-sam)))
        (is (= (slurp-bam tmp-bam) (slurp-bam test-bam)))))

    (testing "BAM -> SAM"
      (let [tmp-sam (.getAbsolutePath (jio/file tmp-dir "out.sam"))]
        (is (zero? (:status (main/exec :sam ["convert" test-bam tmp-sam]))))
        (is (= (slurp-sam tmp-sam) (slurp-bam test-bam)))
        (is (= (slurp-sam tmp-sam) (slurp-sam test-sam)))))

    (testing "FASTA -> TwoBit"
      (let [tmp-twobit (.getAbsolutePath (jio/file tmp-dir "out.2bit"))]
        (is (zero? (:status (main/exec :sam ["convert" test-fasta tmp-twobit]))))
        (is (same-sequence-contents? test-fasta tmp-twobit))))

    (testing "TwoBit -> FASTA"
      (let [tmp-fasta (.getAbsolutePath (jio/file tmp-dir "out.fa"))]
        (is (zero? (:status (main/exec :sam ["convert" test-twobit tmp-fasta]))))
        (is (same-sequence-contents? test-twobit tmp-fasta))))

    (testing "error"
      (let [tmp-unknown (.getAbsolutePath (jio/file tmp-dir "test.unknown"))
            tmp-twobit (.getAbsolutePath (jio/file tmp-dir "error.2bit"))]
        (are [in out] (thrown? Exception (main/exec :sam ["convert" in out]))
          test-bam tmp-unknown
          test-bam tmp-twobit)))))

(deftest sam-normalize-test
  (with-tmp-dir tmp-dir
    (let [tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
      (is (zero? (:status (main/exec :sam ["normalize" test-unnormalized-bam tmp-bam]))))
      (is (same-sam-contents? test-normalized-bam tmp-bam)))))

(deftest sam-sort-test
  (with-tmp-dir tmp-dir
    (testing "sort by pos"
      (let [tmp-sam (.getAbsolutePath (jio/file tmp-dir "out.sam"))
            tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))
            tmp-unknown (.getAbsolutePath (jio/file tmp-dir "out.unknown"))]
        (is (match? {:ret {:status zero?}}
                    (test-call
                     #(main/exec :sam ["sort" "-o" "coordinate"
                                       resource/test-sam tmp-sam]))))
        (is (.exists (jio/file tmp-sam)))

        (is (match? {:ret {:status zero?}}
                    (test-call
                     #(main/exec :sam ["sort" "-o" "coordinate" "-c" "4"
                                       resource/test-bam tmp-bam]))))
        (is (.exists (jio/file tmp-bam)))

        (is (thrown? IllegalArgumentException
                     (test-call
                      #(main/exec :sam ["sort" "-o" "coordinate"
                                        resource/test-fasta tmp-bam]))))
        (is (thrown? IllegalArgumentException
                     (test-call
                      #(main/exec :sam ["sort" "-o" "coordinate" resource/test-bam
                                        tmp-unknown]))))))

    (testing "sort by qname"
      (let [tmp-sam (.getAbsolutePath (jio/file tmp-dir "out.sam"))
            tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
        (is (match? {:ret {:status zero?}}
                    (test-call
                     #(main/exec :sam ["sort" "-o" "queryname" test-sam tmp-sam]))))
        (is (.exists (jio/file tmp-sam)))

        (is (match? {:ret {:status zero?}}
                    (test-call
                     #(main/exec :sam ["sort" "-o" "queryname" test-bam tmp-bam]))))
        (is (.exists (jio/file tmp-bam)))))))

(deftest sam-index-test
  (with-tmp-dir tmp-dir
    (let [tmp-bam (.getAbsolutePath (jio/file tmp-dir "tmp.bam"))]
      (jio/copy (jio/file test-sorted-bam) (jio/file tmp-bam))
      (is (zero? (:status (main/exec :sam ["index" tmp-bam]))))
      (is (.exists (jio/file (str tmp-bam ".bai"))))
      (is (zero? (:status (main/exec :sam ["index" "-t" "1" tmp-bam]))))
      (is (zero? (:status (main/exec :sam ["index" "-t" "4" tmp-bam])))))))

(deftest sam-pileup-test
  ;; NB: "pileup" output format may change in future (maybe)
  (is (match? {:ret {:status zero?}
               :out (slurp resource/test-pileup)}
              (test-call
               #(main/exec :sam ["pileup" resource/test-sorted-bam]))))

  (is (match? {:ret {:status zero?}}
              (test-call
               #(main/exec :sam ["pileup" "-s" resource/test-sorted-bam]))))
  (is (match? {:ret {:status zero?}}
              (test-call
               #(main/exec :sam ["pileup" "-r" "ref2:10-200"
                                 resource/test-sorted-bam]))))
  (is (match? {:ret {:status zero?}}
              (test-call
               #(main/exec :sam ["pileup" "-f" resource/test-fasta
                                 resource/test-sorted-bam]))))
  (is (match? {:ret {:status zero?}}
              (test-call
               #(main/exec :sam ["pileup" "-t" "4" resource/test-sorted-bam])))))

(deftest sam-level-test
  (with-tmp-dir tmp-dir
    (let [tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
      (is (match? {:ret {:status zero?}}
                  (test-call
                   #(main/exec :sam ["level" resource/test-sorted-bam tmp-bam]))))
      (is (.exists (jio/file tmp-bam)))

      (is (thrown? clojure.lang.ExceptionInfo
                   (test-call
                    #(main/exec :sam ["level" resource/test-bam tmp-bam])))))))

(deftest vcf-liftover-test
  (with-tmp-dir tmp-dir
    (let [tmp-vcf (.getAbsolutePath (jio/file tmp-dir "out.vcf"))]
      (testing "success"
        (is (match? {:ret {:status zero?}}
                    (test-call
                     #(main/exec :vcf ["liftover" "--reference" test-lift-fasta
                                       "--chain" test-chain test-unlifted-vcf
                                       tmp-vcf]))))
        (is (= (slurp-vcf test-lifted-vcf) (slurp-vcf tmp-vcf))))

      (testing "failure"
        (is (thrown? Exception
                     (test-call
                      #(main/exec :vcf ["liftover" "--chain" test-chain
                                        test-unlifted-vcf tmp-vcf]))))
        (is (thrown? Exception
                     (test-call
                      #(main/exec :vcf ["liftover" "--reference" test-lift-fasta
                                        test-unlifted-vcf tmp-vcf]))))))))

(deftest variant-liftover-test
  (are [chr pos out] (match? {:ret {:status zero?}
                              :out (m/via string/trim-newline
                                          (str "chr\tpos\n" out))}
                             (test-call
                              #(main/exec :variant ["liftover" "--chain"
                                                    test-chain chr pos])))
    "chr1" "5" "chr1\t56"
    "chr1" "8" "chr1\t53")

  (are [chr pos] (match? {:ret {:status (complement zero?)}}
                         (test-call
                          #(main/exec :variant ["liftover" "--chain" test-chain
                                                chr pos])))
    "chr1" "70"
    "chr2" "5"))

(deftest ^:slow variant-to-hgvs-test
  (prepare-cavia!)
  (testing "success"
    (are [chr pos ref alt out]
         (match? {:ret {:status zero?}
                  :out (m/via string/trim-newline out)}
                 (test-call
                  #(main/exec :variant ["to-hgvs"
                                        "--reference" test-large-reference
                                        "--refseq" test-large-ref-seq
                                        chr pos ref alt])))
      "chr7" "55191822" "T" "G" "NM_005228:c.2573T>G\tp.L858R"
      "chr1" "11796321" "G" "A" "NM_005957:c.665C>T\tp.A222V"))

  (testing "failure"
    (is (thrown? Exception
                 (test-call
                  #(main/exec :variant ["to-hgvs"
                                        "chr7" "55191822" "T" "G"]))))
    (is (thrown? Exception
                 (test-call
                  #(main/exec :variant ["to-hgvs"
                                        "--reference" test-large-reference
                                        "chr7" "55191822" "T" "G"]))))
    (is (thrown? Exception
                 (test-call
                  #(main/exec :variant ["to-hgvs"
                                        "--refseq" test-large-ref-seq
                                        "chr7" "55191822" "T" "G"]))))))

(deftest hgvs-format-test
  (are [args out] (match? {:ret {:status zero?}
                           :out (m/via string/trim-newline out)}
                          (test-call #(main/exec :hgvs (into ["format"] args))))
    ["NG_012232.1:g.19_21delTGC"] "NG_012232.1:g.19_21del"
    ["g.6775delTinsGA"] "g.6775delinsGA"
    ["p.I327Rfs*?"] "p.Ile327Argfs"
    ["p.*110Glnext*17"] "p.Ter110Glnext*17"

    ["--amino-acid-format" "short" "NP_005219.2:p.Leu858Arg"] "NP_005219.2:p.L858R"
    ["--ter-format" "short" "p.Ter110Glnext*17"] "p.*110Glnext*17")

  (are [args] (thrown? Exception (test-call
                                  #(main/exec :hgvs (into ["format"] args))))
    [":2361G>A"]
    ["NM_005228.3:2361G>A"]
    [""]))

(deftest hgvs-repair-test
  (are [in out] (match? {:ret {:status zero?}
                         :out (m/via string/trim-newline out)}
                        (test-call #(main/exec :hgvs ["repair" in])))
    "NM_00001.1:c.123_124 delinsCGT" "NM_00001.1:c.123_124delinsCGT"
    "c.123_124GC>AA" "c.123_124delGCinsAA"
    "p.Phe269Phe=" "p.Phe269=")

  (are [in] (match? {:ret {:status zero?}
                     :out (m/via string/trim-newline in)}
                    (test-call #(main/exec :hgvs ["repair" in])))
    "NM_005228.3:c.2361G>A"
    "c.2361G>A"
    "NP_005219.2:p.Leu858Arg"
    "NP_005219.2:p.L858R"
    ""))

(deftest ^:slow hgvs-to-variant-test
  (prepare-cavia!)
  (testing "success"
    (are [hgvs out]
         (match? {:ret {:status zero?}
                  :out (m/via string/trim-newline
                              (str "chr\tpos\tref\talt\n" out))}
                 (test-call
                  #(main/exec :hgvs ["to-variant"
                                     "--reference" test-large-reference
                                     "--refseq" test-large-ref-seq
                                     hgvs])))
      "NM_005228:c.2573T>G" "chr7\t55191822\tT\tG"
      "NM_005957:c.665C>T" "chr1\t11796321\tG\tA"))

  (testing "failure"
    (is (thrown? Exception
                 (test-call
                  #(main/exec :hgvs ["to-variant" "NM_005228:c.2573T>G"]))))
    (is (thrown? Exception
                 (test-call
                  #(main/exec :hgvs ["to-variant"
                                     "--reference" test-large-reference
                                     "NM_005228:c.2573T>G"]))))
    (is (thrown? Exception
                 (test-call
                  #(main/exec :hgvs ["to-variant"
                                     "--refseq" test-large-ref-seq
                                     "NM_005228:c.2573T>G"]))))))

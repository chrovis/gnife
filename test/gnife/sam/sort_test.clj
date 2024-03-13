(ns gnife.sam.sort-test
  (:require [cljam.algo.sorter :as sorter]
            [cljam.io.protocols :as cprotocols]
            [clojure.java.io :as jio]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [gnife.sam.sort :as sort]
            [gnife.test :refer [disable-log-fixture slurp-sam slurp-bam with-tmp-dir]]
            [gnife.test.resource :as resource]))

(use-fixtures :once disable-log-fixture)

(def ^:private to-sam-alignment
  (comp
   cprotocols/map->SAMAlignment
   #(update % :flag int)
   #(update % :pos int)
   #(update % :end int)
   #(update % :mapq int)
   #(update % :pnext int)
   #(update % :tlen int)))

(def ^:private test-sam-sorted-by-pos
  {:header {:HD {:VN "1.4", :SO "coordinate"}
            :SQ [{:SN "ref", :LN 45} {:SN "ref2", :LN 40}]}
   :alignments
   (->>
    [{:qname "r001", :flag 163, :rname "ref" , :pos 7 , :end 22, :mapq 30, :cigar "8M4I4M1D3M"        , :rnext "=", :pnext 37, :tlen 39 , :seq "TTAGATAAAGAGGATACTG"       , :qual "*"                         , :options [{:XX {:type "B", :value "S,12561,2,20,112"}}]}
     {:qname "r002", :flag 0  , :rname "ref" , :pos 9 , :end 18, :mapq 30, :cigar "1S2I6M1P1I1P1I4M2I", :rnext "*", :pnext 0 , :tlen 0  , :seq "AAAAGATAAGGGATAAA"         , :qual "*"                         , :options []}
     {:qname "r003", :flag 0  , :rname "ref" , :pos 9 , :end 14, :mapq 30, :cigar "5H6M"              , :rnext "*", :pnext 0 , :tlen 0  , :seq "AGCTAA"                    , :qual "*"                         , :options []}
     {:qname "r004", :flag 0  , :rname "ref" , :pos 16, :end 40, :mapq 30, :cigar "6M14N1I5M"         , :rnext "*", :pnext 0 , :tlen 0  , :seq "ATAGCTCTCAGC"              , :qual "*"                         , :options []}
     {:qname "r003", :flag 16 , :rname "ref" , :pos 29, :end 33, :mapq 30, :cigar "6H5M"              , :rnext "*", :pnext 0 , :tlen 0  , :seq "TAGGC"                     , :qual "*"                         , :options []}
     {:qname "r001", :flag 83 , :rname "ref" , :pos 37, :end 45, :mapq 30, :cigar "9M"                , :rnext "=", :pnext 7 , :tlen -39, :seq "CAGCGCCAT"                 , :qual "*"                         , :options []}
     {:qname "x1"  , :flag 0  , :rname "ref2", :pos 1 , :end 20, :mapq 30, :cigar "20M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "AGGTTTTATAAAACAAATAA"      , :qual "????????????????????"      , :options []}
     {:qname "x2"  , :flag 0  , :rname "ref2", :pos 2 , :end 22, :mapq 30, :cigar "21M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "GGTTTTATAAAACAAATAATT"     , :qual "?????????????????????"     , :options []}
     {:qname "x3"  , :flag 0  , :rname "ref2", :pos 6 , :end 27, :mapq 30, :cigar "9M4I13M"           , :rnext "*", :pnext 0 , :tlen 0  , :seq "TTATAAAACAAATAATTAAGTCTACA", :qual "??????????????????????????", :options []}
     {:qname "x4"  , :flag 0  , :rname "ref2", :pos 10, :end 34, :mapq 30, :cigar "25M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "CAAATAATTAAGTCTACAGAGCAAC" , :qual "?????????????????????????" , :options []}
     {:qname "x5"  , :flag 0  , :rname "ref2", :pos 12, :end 35, :mapq 30, :cigar "24M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "AATAATTAAGTCTACAGAGCAACT"  , :qual "????????????????????????"  , :options []}
     {:qname "x6"  , :flag 0  , :rname "ref2", :pos 14, :end 36, :mapq 30, :cigar "23M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "TAATTAAGTCTACAGAGCAACTA"   , :qual "???????????????????????"   , :options []}]
    (map to-sam-alignment))})

(def test-sam-sorted-by-qname
  {:header {:HD {:VN "1.4", :SO "queryname"}
            :SQ [{:SN "ref", :LN 45} {:SN "ref2", :LN 40}]}
   :alignments
   (->>
    [{:qname "r001", :flag 83 , :rname "ref" , :pos 37, :end 45, :mapq 30, :cigar "9M"                , :rnext "=", :pnext 7 , :tlen -39, :seq "CAGCGCCAT"                 , :qual "*"                         , :options []}
     {:qname "r001", :flag 163, :rname "ref" , :pos 7 , :end 22, :mapq 30, :cigar "8M4I4M1D3M"        , :rnext "=", :pnext 37, :tlen 39 , :seq "TTAGATAAAGAGGATACTG"       , :qual "*"                         , :options [{:XX {:type "B", :value "S,12561,2,20,112"}}]}
     {:qname "r002", :flag 0  , :rname "ref" , :pos 9 , :end 18, :mapq 30, :cigar "1S2I6M1P1I1P1I4M2I", :rnext "*", :pnext 0 , :tlen 0  , :seq "AAAAGATAAGGGATAAA"         , :qual "*"                         , :options []}
     {:qname "r003", :flag 16 , :rname "ref" , :pos 29, :end 33, :mapq 30, :cigar "6H5M"              , :rnext "*", :pnext 0 , :tlen 0  , :seq "TAGGC"                     , :qual "*"                         , :options []}
     {:qname "r003", :flag 0  , :rname "ref" , :pos 9 , :end 14, :mapq 30, :cigar "5H6M"              , :rnext "*", :pnext 0 , :tlen 0  , :seq "AGCTAA"                    , :qual "*"                         , :options []}
     {:qname "r004", :flag 0  , :rname "ref" , :pos 16, :end 40, :mapq 30, :cigar "6M14N1I5M"         , :rnext "*", :pnext 0 , :tlen 0  , :seq "ATAGCTCTCAGC"              , :qual "*"                         , :options []}
     {:qname "x1"  , :flag 0  , :rname "ref2", :pos 1 , :end 20, :mapq 30, :cigar "20M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "AGGTTTTATAAAACAAATAA"      , :qual "????????????????????"      , :options []}
     {:qname "x2"  , :flag 0  , :rname "ref2", :pos 2 , :end 22, :mapq 30, :cigar "21M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "GGTTTTATAAAACAAATAATT"     , :qual "?????????????????????"     , :options []}
     {:qname "x3"  , :flag 0  , :rname "ref2", :pos 6 , :end 27, :mapq 30, :cigar "9M4I13M"           , :rnext "*", :pnext 0 , :tlen 0  , :seq "TTATAAAACAAATAATTAAGTCTACA", :qual "??????????????????????????", :options []}
     {:qname "x4"  , :flag 0  , :rname "ref2", :pos 10, :end 34, :mapq 30, :cigar "25M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "CAAATAATTAAGTCTACAGAGCAAC" , :qual "?????????????????????????" , :options []}
     {:qname "x5"  , :flag 0  , :rname "ref2", :pos 12, :end 35, :mapq 30, :cigar "24M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "AATAATTAAGTCTACAGAGCAACT"  , :qual "????????????????????????"  , :options []}
     {:qname "x6"  , :flag 0  , :rname "ref2", :pos 14, :end 36, :mapq 30, :cigar "23M"               , :rnext "*", :pnext 0 , :tlen 0  , :seq "TAATTAAGTCTACAGAGCAACTA"   , :qual "???????????????????????"   , :options []}]
    (map to-sam-alignment))})

(defn- uniq [coll]
  (reduce
   (fn [r one]
     (if (= (first r) one)
       r
       (conj r one)))
   nil
   coll))

(defn- get-rnames [sam]
  (uniq (map :rname (:alignments sam))))

(defn- correct-sort-order?
  [target-sam & [contrast-sam]]
  (let [target-rnames (get-rnames target-sam)]
    (if (and contrast-sam (not= target-rnames (get-rnames contrast-sam)))
      false
      (->> target-rnames
           (map (fn [rname]
                  (try
                    (->> (:alignments target-sam)
                         (filter #(= rname (:rname %)))
                         (reduce (fn [prev one]
                                   (case (compare (:pos prev) (:pos one))
                                     -1 true
                                     1 (throw (Exception. "pos not sorted"))
                                     (case (compare (bit-and 16 (:flag prev))
                                                    (bit-and 16 (:flag one)))
                                       -1 true
                                       1 (throw (Exception. "reverse flag not sorted"))
                                       true))
                                   one)))
                    true
                    (catch Exception _
                      false))))
           (every? true?)))))

(deftest sort-test
  (with-tmp-dir tmp-dir
    (testing "sort by pos"
      (let [tmp-sam (.getAbsolutePath (jio/file tmp-dir "out.sam"))
            tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))
            tmp-unknown (.getAbsolutePath (jio/file tmp-dir "out.unknown"))]
        (is (zero? (:status (sort/sort [resource/test-sam tmp-sam]
                                       {:order "coordinate"
                                        :chunk sorter/default-chunk-size}))))
        (is (= (slurp-sam tmp-sam) test-sam-sorted-by-pos))
        (is (correct-sort-order? (slurp-sam tmp-sam) test-sam-sorted-by-pos))

        (is (zero? (:status (sort/sort [resource/test-bam tmp-bam]
                                       {:order "coordinate"
                                        :chunk 4}))))
        (is (= (slurp-bam tmp-bam) test-sam-sorted-by-pos))
        (is (correct-sort-order? (slurp-bam tmp-bam) test-sam-sorted-by-pos))

        (is (thrown? IllegalArgumentException
                     (sort/sort [resource/test-fasta tmp-bam]
                                {:order "coordinate"
                                 :chunk sorter/default-chunk-size})))
        (is (thrown? IllegalArgumentException
                     (sort/sort [resource/test-bam tmp-unknown]
                                {:order "coordinate"
                                 :chunk sorter/default-chunk-size})))))

    (testing "sort by qname"
      (let [tmp-sam (.getAbsolutePath (jio/file tmp-dir "out.sam"))
            tmp-bam (.getAbsolutePath (jio/file tmp-dir "out.bam"))]
        (is (zero? (:status (sort/sort [resource/test-sam tmp-sam]
                                       {:order "queryname"
                                        :chunk sorter/default-chunk-size}))))
        (is (= (slurp-sam tmp-sam) test-sam-sorted-by-qname))

        (is (zero? (:status (sort/sort [resource/test-bam tmp-bam]
                                       {:order "queryname"
                                        :chunk sorter/default-chunk-size}))))
        (is (= (slurp-bam tmp-bam) test-sam-sorted-by-qname))))))

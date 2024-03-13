(ns gnife.test.resource
  (:require [cavia.core :as cavia]))

(cavia/defprofile cavia-prof
  {:resources [{:id "hg38.2bit"
                :url "https://test.chrov.is/data/varity/hg38.2bit"
                :sha1 "6fb20ba4de0b49247b78e08c2394d0c4f8594148"}
               {:id "hg38-refGene.txt.gz"
                :url "https://test.chrov.is/data/varity/hg38-refGene.txt.gz"
                :sha1 "941d514e57f4e842743f5c9269a0279906a072a0"}]})

(defn prepare-cavia!
  []
  (cavia/with-verbosity {:message false}
    (cavia/get! cavia-prof)))

(def test-fasta "resources/sequence/test.fa")
(def test-twobit "resources/sequence/test.2bit")
(def test-lift-fasta "resources/sequence/test-lift.fa")
(def test-large-reference (cavia/resource cavia-prof "hg38.2bit"))

(def test-sam "resources/sam/test.sam")
(def test-bam "resources/sam/test.bam")
(def test-sorted-bam "resources/sam/test.sorted.bam")
(def test-unnormalized-bam "resources/sam/test-unnormalized.bam")
(def test-normalized-bam "resources/sam/test-normalized.bam")

(def test-pileup "resources/sam/pileup/test.pileup")

(def test-unlifted-vcf "resources/vcf/test-unlifted.vcf")
(def test-lifted-vcf "resources/vcf/test-lifted.vcf")

(def test-large-ref-seq (cavia/resource cavia-prof "hg38-refGene.txt.gz"))

(def test-chain "resources/chain/test.chain")

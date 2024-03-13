(ns gnife.test
  (:require [cljam.io.sam :as io.sam]
            [cljam.io.sequence :as io.sequence]
            [cljam.io.vcf :as io.vcf]
            [clojure.java.io :as jio]
            [clojure.tools.logging :refer [*logger-factory*]]
            [clojure.tools.logging.impl :refer [disabled-logger-factory]])
  (:import java.nio.file.Files
           java.nio.file.attribute.FileAttribute))

(defmacro with-out-noop
  [& body]
  `(let [w# (proxy [java.io.Writer] []
              (close [] nil)
              (flush [] nil)
              (write
                ([_#] nil)
                ([_# _# _#] nil)))]
     (binding [*err* w#
               *out* w#]
       ~@body)))

(defmacro with-tmp-dir
  [dir & body]
  `(let [~dir (doto (.toFile
                     (Files/createTempDirectory "gnife-test"
                                                (into-array FileAttribute [])))
                (.deleteOnExit))]
     (try
       ~@body
       (finally
         (when (.exists ~dir)
           (doseq [f# (seq (.list ~dir))]
             (.delete (jio/file ~dir f#)))
           (.delete ~dir))))))

(defn disable-log-fixture
  "A fixture function to suppress log output. Call as
  (use-fixtures :once disable-log-fixture) in a test namespace."
  [f]
  (binding [*logger-factory* disabled-logger-factory]
    (f)))

(defn same-sequence-contents?
  "Returns true if the contents of two FASTA/TwoBit files are equivalent, false
  if not."
  [f1 f2]
  (with-open [r1 (io.sequence/reader f1)
              r2 (io.sequence/reader f2)]
    (= (io.sequence/read-all-sequences r1)
       (io.sequence/read-all-sequences r2))))

(defn slurp-sam
  [f]
  (with-open [r (io.sam/sam-reader f)]
    {:header (io.sam/read-header r)
     :alignments (doall (seq (io.sam/read-alignments r {})))}))

(defn slurp-bam
  [f]
  (with-open [r (io.sam/bam-reader f)]
    {:header (io.sam/read-header r)
     :alignments (doall (seq (io.sam/read-alignments r {})))}))

(defn same-sam-contents?
  "Returns true if the contents of two SAM/BAM files are equivalent, false if
  not."
  [f1 f2]
  (with-open [r1 (io.sam/reader f1)
              r2 (io.sam/reader f2)]
    (and (= (io.sam/read-header r1)
            (io.sam/read-header r2))
         (= (seq (io.sam/read-alignments r1))
            (seq (io.sam/read-alignments r2))))))

(defn slurp-vcf
  [f]
  (with-open [r (io.vcf/reader f)]
    {:meta-info (io.vcf/meta-info r)
     :header (io.vcf/header r)
     :variants (doall (io.vcf/read-variants r))}))

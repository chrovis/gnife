(ns gnife.vcf.liftover
  (:require [cljam.io.vcf :as vcf]
            [cljam.io.sequence :as cseq]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]
            [varity.chain :as ch]
            [varity.vcf-lift :as vcf-lift]))

(defn- liftover
  [[in-vcf out-vcf] {:keys [reference chain]}]
  (with-open [vcf-rdr (vcf/reader in-vcf)
              vcf-wtr (vcf/writer out-vcf
                                  (vcf/meta-info vcf-rdr)
                                  (vcf/header vcf-rdr))
              seq-rdr (cseq/reader reference)]
    (->> (vcf/read-variants vcf-rdr)
         (vcf-lift/liftover-variants seq-rdr
                                     (ch/index (ch/load-chain chain)))
         :success
         (vcf/write-variants vcf-wtr)))
  {:status 0})

(def description
  "Convert genomic coordinates in a VCF file between assemblies")

(def options
  [[nil "--reference REFERENCE" "Reference sequence file"]
   [nil "--chain CHAIN" "Liftover chain file"]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife vcf liftover --reference <reference> --chain <chain> <in.vcf> <out.vcf>"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (= (count arguments) 2)
      {:arguments arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (liftover arguments options))))

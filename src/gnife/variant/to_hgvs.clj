(ns gnife.variant.to-hgvs
  (:require [clj-hgvs.core :as hgvs]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]
            [varity.vcf-to-hgvs :as v2h]))

(def ^:private fmtopts
  {:show-bases? true
   :amino-acid-format :short
   :show-ter-site? true
   :ter-format :short})

(defn- to-hgvs
  [[chr pos ref alt] {:keys [reference refseq]}]
  (doseq [{:keys [coding-dna protein]} (v2h/vcf-variant->hgvs
                                        {:chr chr, :pos (parse-long pos), :ref ref, :alt alt}
                                        reference refseq)]
    (->> [(hgvs/format coding-dna fmtopts)
          (when protein
            (hgvs/format protein fmtopts))]
         (string/join \tab)
         println))
  {:status 0})

(def description
  "Convert a VCF-style variant into HGVS")

(def options
  [[nil "--reference REFERENCE" "Reference sequence file"]
   [nil "--refseq REFSEQ" "RefSeq file"]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife variant to-hgvs [<options>] <chr> <pos> <ref> <alt>"
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

      (= (count arguments) 4)
      {:arguments arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (to-hgvs arguments options))))

(ns gnife.hgvs.to-variant
  (:require [clj-hgvs.core :as clj-hgvs]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]
            [varity.hgvs-to-vcf :as h2v]))

(defn- to-vcf-variant
  [hgvs {:keys [reference refseq]}]
  (let [variants (h2v/hgvs->vcf-variants
                  (clj-hgvs/parse hgvs) reference refseq)]
    (if (seq variants)
      (do (println (string/join \tab ["chr" "pos" "ref" "alt"]))
          (doseq [{:keys [chr pos ref alt]} variants]
            (println (string/join \tab [chr pos ref alt])))
          {:status 0})
      {:status 1})))

(def description
  "Convert a HGVS into VCF-style variants")

(def options
  [[nil "--reference REFERENCE" "Reference sequence file"]
   [nil "--refseq REFSEQ" "RefSeq file"]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife hgvs to-variant [<options>] <HGVS>"
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

      (= (count arguments) 1)
      {:hgvs (first arguments) :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [hgvs options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (to-vcf-variant hgvs options))))

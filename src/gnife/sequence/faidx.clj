(ns gnife.sequence.faidx
  (:require [cljam.algo.fasta-indexer :as fai]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn faidx
  [[fasta]]
  (fai/create-index fasta (str fasta ".fai"))
  {:status 0})

(def description
  "Index a reference sequence in the FASTA format")

(def options
  [["-h" "--help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sequence faidx <ref.fasta>"
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
      {:arguments arguments}

      :else
      {:exit-message (usage summary)})))

(defn run [args]
  (let [{:keys [arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (faidx arguments))))

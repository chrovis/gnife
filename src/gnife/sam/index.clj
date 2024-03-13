(ns gnife.sam.index
  (:require [cljam.algo.bam-indexer :as bai]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn index
  [[bam] options]
  (bai/create-index bam (str bam ".bai") :n-threads (:thread options))
  {:status 0})

(def description
  "Index sorted alignment for fast random access")

(def options
  [["-t" "--thread THREAD" "Number of threads (0 is auto)"
    :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam index [-t THREAD] <in.bam>"
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
      {:arguments arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn run
  [args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (index arguments options))))

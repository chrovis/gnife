(ns gnife.sam.level
  (:require [cljam.algo.level :as level]
            [cljam.io.sam :as sam]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn level
  [[in out]]
  (with-open [r (sam/reader in)
              w (sam/writer out)]
    (level/add-level r w))
  {:status 0})

(def description
  "Analyze a BAM file and add level information of alignments")

(def options
  [["-h" "--help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam level <in.bam> <out.bam>"
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
      {:arguments arguments}

      :else
      {:exit-message (usage summary)})))

(defn run [args]
  (let [{:keys [arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (level arguments))))

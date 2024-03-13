(ns gnife.sam.normalize
  (:require [cljam.algo.normal :as normal]
            [cljam.io.sam :as sam]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn normalize
  [[in out]]
  (with-open [r (sam/reader in)
              w (sam/writer out)]
    (normal/normalize r w))
  {:status 0})

(def description
  "Normalize references of alignments")

(def options
  [["-h" "--help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam normalize <in.bam|sam> <out.bam|sam>"
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
      (normalize arguments))))

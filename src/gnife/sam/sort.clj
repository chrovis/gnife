(ns gnife.sam.sort
  (:refer-clojure :exclude [sort])
  (:require [cljam.algo.sorter :as sorter]
            [cljam.io.sam :as sam]
            [cljam.io.sam.util.header :as header]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn sort
  [[in out] options]
  (with-open [r (sam/reader in)
              w (sam/writer out)]
    (condp = (:order options)
      (name header/order-coordinate) (sorter/sort-by-pos r w {:chunk-size (:chunk options)})
      (name header/order-queryname) (sorter/sort-by-qname r w {:chunk-size (:chunk options)})))
  {:status 0})

(def description
  "Sort alignments by leftmost coordinates")

(def options
  [["-o" "--order ORDER" "Sorting order of alignments <coordinate|queryname>"
    :default "coordinate"]
   ["-c" "--chunk CHUNK" "Maximum number of alignments sorted per thread."
    :default sorter/default-chunk-size
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam sort [-o ORDER] [-c CHUNK] <in.bam|sam> <out.bam|sam>"
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
      {:files arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn run
  [args]
  (let [{:keys [files options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (sort files options))))

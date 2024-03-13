(ns gnife.sam.view
  (:require [cljam.io.sam :as sam]
            [cljam.io.sam.util :as sam-util]
            [cljam.io.sam.util.header :as header]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg parse-region]])
  (:import [java.io Closeable]))

(defn view
  [sam options]
  (with-open [^Closeable r (condp = (:format options)
                             "auto" (sam/reader sam)
                             "sam"  (sam/sam-reader sam)
                             "bam"  (sam/bam-reader sam))]
    (when (:header options)
      (println (header/stringify-header (sam/read-header r))))
    (let [alns (if-let [region (parse-region (:region options))]
                 (if (sam/indexed? r)
                   (sam/read-alignments r region)
                   :unindexed)
                 (sam/read-alignments r))]
      (if (= alns :unindexed)
        {:status 1
         :message "Random alignment retrieval only works for indexed BAM."}
        (doseq [aln alns]
          (println (sam-util/stringify-alignment aln))))))
  {:status 0})

(def description
  "Extract/print all or sub alignments in SAM or BAM format")

(def options
  [[nil "--header" "Include header"]
   ["-f" "--format FORMAT" "Input file format <auto|sam|bam>"
    :default "auto"]
   ["-r" "--region REGION" "Only print in region (e.g. chr6:1000-2000)"]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam view [--header] [-f FORMAT] [-r REGION] <in.bam|sam>"
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
      {:sam (first arguments) :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [sam options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (view sam options))))

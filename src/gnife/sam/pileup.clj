(ns gnife.sam.pileup
  (:require [cljam.algo.depth :as depth]
            [cljam.algo.pileup :as plp]
            [cljam.algo.sorter :as sorter]
            [cljam.io.sam :as sam]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg parse-region]]))

(defn- depth
  [f region n-threads]
  (with-open [r (sam/reader f)]
    (cond
      (not (sam/indexed? r))
      {:status 1, :message "Random alignment retrieval only works for indexed BAM."}

      (not (sorter/sorted? r))
      {:status 1, :message "Not sorted"}

      :else
      (let [regs (or (some-> region parse-region vector)
                     (map (fn [{:keys [name len]}] {:chr name :start 1 :end len}) (sam/read-refs r)))]
        (binding [*flush-on-newline* false]
          (doseq [reg regs
                  line (depth/lazy-depth r reg {:n-threads n-threads})]
            (println line))
          (flush))
        {:status 0}))))

(defn pileup
  [[bam] options]
  (if (:simple options)
    (depth bam (:region options) (:thread options))
    (do (plp/create-mpileup bam (:ref options) *out*
                            (parse-region (:region options)))
        {:status 0})))

(def description
  "Generate pileup for the BAM file")

(def options
  [["-s" "--simple" "Output only pileup count."]
   ["-r" "--region REGION" "Only pileup in region. (e.g. chr6:1000-2000)"]
   ["-t" "--thread THREAD" "Number of threads (0 is auto)"
    :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-f" "--ref FASTA" "Reference file in the FASTA format."
    :default nil]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam pileup [-s] [-r REGION] [-f FASTA] [-t THREAD] <in.bam>"
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
      (pileup arguments options))))

(ns gnife.hgvs.format
  (:refer-clojure :exclude [format])
  (:require [clj-hgvs.core :as hgvs]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn- format
  [hgvs options]
  (println (hgvs/format (hgvs/parse hgvs) options))
  {:status 0})

(def description
  "Format HGVS with a specified style")

(def options
  [[nil "--show-bases" "Display additional bases, e.g. g.6_8delTGC"]
   [nil "--ins-format <auto|bases|count>" "Bases style of insertion"
    :default :auto
    :default-desc "auto"
    :parse-fn keyword
    :validate [#{:auto :bases :count} "Must be one of <auto|bases|count>"]]
   [nil "--range-format <auto|bases|coord>" "Range style"
    :default :auto
    :default-desc "auto"
    :parse-fn keyword
    :validate [#{:auto :bases :coord} "Must be one of <auto|bases|coord>"]]
   [nil "--amino-acid-format <long|short>" "Amino acid style of protein HGVS"
    :default :long
    :default-desc "long"
    :parse-fn keyword
    :validate [#{:long :short} "Must be one of <long|short>"]]
   [nil "--show-ter-site" "Display a new ter codon site of protein frame shift"]
   [nil "--ter-format <long|short>" "Ter codon style of protein frame shift and extension"
    :default :long
    :default-desc "long"
    :parse-fn keyword
    :validate [#{:long :short} "Must be one of <long|short>"]]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife hgvs format [<options>] <HGVS>"
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
      (format hgvs options))))

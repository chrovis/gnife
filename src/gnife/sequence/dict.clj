(ns gnife.sequence.dict
  (:require [cljam.io.dict.core :as dict.core]
            [cljam.io.fasta.core :as fa.core]
            [cljam.io.sequence :as cseq]
            [clojure.java.io :as jio]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]
            [gnife.util :as util]))

(defn- dict
  [in {:keys [output]}]
  (let [out (or output (str (util/corepath in) ".dict"))
        ur (str (.. (jio/file in) getCanonicalFile toURI))]
    (with-open [r (cseq/fasta-reader in)]
      (dict.core/create-dict out
                             (fa.core/read-headers r)
                             (cseq/read-all-sequences r {:mask? true})
                             ur)))
  {:status 0})

(def description
  "Create a sequence dictionary for a reference sequence")

(def options
  [["-o" "--output ref.dict" "Output dict file. The input reference with .dict extension will be used by default."]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sequence dict [--output ref.dict] <ref.fasta>"
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
      {:in (first arguments) :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [in options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (dict in options))))

(ns gnife.sam.convert
  (:require [cljam.algo.convert :as convert]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]))

(defn convert
  [[in & [out & more :as outs]] {:keys [thread]}]
  (convert/convert in (if more outs out) :n-threads thread)
  {:status 0})

(def description
  "Convert file format based on the file extension")

(def options
  [["-t" "--thread THREAD" "Number of threads (0 is auto)"
    :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife sam convert [-t THREAD] <in-file> <out-file> [<out-file-2> [<out-file-3>]]"
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

      (<= 2 (count arguments) 4)
      {:files arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn run [args]
  (let [{:keys [files options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (convert files options))))

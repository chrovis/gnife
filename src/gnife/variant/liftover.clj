(ns gnife.variant.liftover
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [gnife.common :refer [error-msg]]
            [varity.lift :as lift]))

(defn- liftover
  [[chr pos] {:keys [chain]}]
  (if-let [{:keys [chr pos]} (lift/convert-coord {:chr chr ,:pos (parse-long pos)}
                                                 chain)]
    (do (println (str "chr\tpos\n" chr \tab pos))
        {:status 0})
    {:status 1, :message "A lifted-over coordinate is not found"}))

(def description
  "Convert a genomic coordinate between assemblies")

(def options
  [[nil "--chain CHAIN" "Liftover chain file"]
   ["-h" "--help" "Print help"]])

(defn usage [options-summary]
  (->> [description
        ""
        "Usage: gnife variant liftover --chain <chain> <chr> <pos>"
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
      {:arguments arguments :options options}

      :else
      {:exit-message (usage summary)})))

(defn exec
  [args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      {:status (if ok? 0 1), :message exit-message}
      (liftover arguments options))))

(ns gnife.sequence.main
  (:require [clj-sub-command.core :as cmd]
            [clojure.string :as string]
            [gnife.common :refer [error-msg exit]]
            [gnife.sequence.dict :as sequence.dict]
            [gnife.sequence.faidx :as sequence.faidx]))

(def options
  [["-h" "--help" "Print help"]])

(def commands
  [["dict" sequence.dict/description]
   ["faidx" sequence.faidx/description]])

(defn usage [options-summary commands-summary]
  (->> ["Usage: gnife sequence [--help] <command> [<args>]"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        commands-summary]
       (string/join \newline)))

(defn validate-args
  [args]
  (let [{:keys [options command arguments errors options-summary commands-summary]}
        (cmd/parse-cmds args options commands :allow-empty-command true)]
    (cond
      (:help options)
      {:exit-message (usage options-summary commands-summary), :ok? true}

      errors
      {:exit-message (error-msg errors)}

      command
      {:command command, :arguments arguments}

      :else
      {:exit-message (usage options-summary commands-summary)})))

(defn exec
  [args]
  (let [{:keys [command arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case command
        :dict (sequence.dict/exec arguments)
        :faidx (sequence.faidx/run arguments)))))

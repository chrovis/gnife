(ns gnife.sam.main
  (:require [clj-sub-command.core :as cmd]
            [clojure.string :as string]
            [gnife.common :refer [error-msg]]
            (gnife.sam [convert :as convert]
                       [index :as index]
                       [level :as level]
                       [normalize :as normalize]
                       [pileup :as pileup]
                       [sort :as sort]
                       [view :as view])))

(def options
  [["-h" "--help" "Print help"]])

(def commands
  [["view" view/description]
   ["convert" convert/description]
   ["normalize" normalize/description]
   ["sort" sort/description]
   ["index" index/description]
   ["pileup" pileup/description]
   ["level" level/description]])

(defn usage [options-summary commands-summary]
  (->> ["Usage: gnife sam [--help] <command> [<args>]"
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
      {:status (if ok? 0 1), :message exit-message}
      (case command
        :view (view/exec arguments)
        :convert   (convert/run arguments)
        :normalize (normalize/run arguments)
        :sort      (sort/run arguments)
        :index     (index/run arguments)
        :pileup    (pileup/run arguments)
        :level     (level/run arguments)))))

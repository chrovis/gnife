(ns gnife.common
  (:require [cljam.util.region :as region]
            [clojure.string :as string]))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit
  [status message]
  (when message
    (binding [*out* (if (zero? status) *out* *err*)]
      (println message)))
  (System/exit status))

(defn parse-region
  [region-str]
  (when region-str
    (if-let [reg (region/parse-region region-str)]
      reg
      (exit 1 (str "Invalid region format: " region-str)))))

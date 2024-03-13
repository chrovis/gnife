(ns gnife.util
  (:require [clojure.java.io :as jio]))

(defn corename
  [file]
  (let [filename (.getName (jio/file file))]
    (if-let [[_ filename-without-ext] (re-matches #"(.+)\.[-\w]+" filename)]
      filename-without-ext
      filename)))

(defn corepath
  [file]
  (if-let [dir (.getParentFile (jio/file file))]
    (str (jio/file dir (corename file)))
    (corename file)))

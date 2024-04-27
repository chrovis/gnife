(ns build
  (:require [clojure.java.io :as jio]
            [clojure.tools.build.api :as b])
  (:import (java.nio.file Files)
           (java.nio.file.attribute PosixFilePermissions)))

(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def uber-file "target/gnife.jar")
(def bin-file "target/gnife")

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/write-file {:path (str class-dir "/VERSION")
                 :string version})
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'gnife.main}))

(def ^String preamble
  "#!/usr/bin/env bash
exec java $GNIFE_JVM_OPTS -jar $0 \"$@\"
goto :eof
")

(defn bin [_]
  (uber nil)
  (jio/make-parents bin-file)
  (with-open [out (jio/output-stream bin-file)]
    (.write out (.getBytes preamble))
    (jio/copy (jio/file uber-file) out))
  (Files/setPosixFilePermissions (.toPath (jio/file bin-file))
                                 (PosixFilePermissions/fromString "rwxr-xr-x")))

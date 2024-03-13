(ns gnife.main
  (:require [clj-sub-command.core :as cmd]
            [clojure.java.io :as jio]
            [clojure.string :as string]
            [gnife.common :refer [error-msg exit]]
            [gnife.hgvs.main :as gnife.hgvs]
            [gnife.sam.main :as gnife.sam]
            [gnife.sequence.main :as gnife.sequence]
            [gnife.variant.main :as gnife.variant]
            [gnife.vcf.main :as gnife.vcf])
  (:gen-class))

(def options
  [["-h" "--help" "Print help"]
   [nil "--tree" "Print the command tree"]
   ["-v" "--version" "Print version"]])

(def commands
  [["sequence" "Manipulate sequence files (e.g., FASTA)"]
   ["sam" "Manipulate SAM files (e.g., SAM and BAM)"]
   ["vcf" "Manipulate VCF files"]
   ["variant" "Manipulate VCF-style variants: chr, pos, ref, and alt"]
   ["hgvs" "Manipulate HGVS"]])

(defn usage [options-summary commands-summary]
  (->> ["gnife is a CLI tool for manipulating genomic files and data."
        ""
        "Usage: gnife [<options>] <type> <command> [<args>]"
        ""
        "Options:"
        options-summary
        ""
        "Types:"
        commands-summary
        ""
        "gnife requires you have installed Java. Set the GNIFE_JVM_OPTS environment"
        "variable to pass JVM options, e.g. GNIFE_JVM_OPTS=\"-Xmx4g\"."]
       (string/join \newline)))

(defn version
  []
  (slurp (jio/resource "VERSION")))

(defn tree
  []
  (string/trim-newline
   (with-out-str
     (doseq [[cmd1] commands]
       (println cmd1)
       (let [commands2 (-> (symbol (str "gnife." cmd1 ".main") "commands")
                           resolve
                           var-get)
             max-len (->> (map first commands2)
                          (map count)
                          (apply max))]
         (doseq [[cmd2 dsc2] commands2]
           (println (format (str "  %-" max-len "s  %s")
                            cmd2 dsc2))))
       (newline)))))

(defn validate-args
  [args]
  (let [{:keys [options command arguments errors options-summary commands-summary]}
        (cmd/parse-cmds args options commands :allow-empty-command true)]
    (cond
      (:version options)
      {:exit-message (version), :ok? true}

      (:tree options)
      {:exit-message (tree), :ok? true}

      (:help options)
      {:exit-message (usage options-summary commands-summary), :ok? true}

      errors
      {:exit-message (error-msg errors)}

      command
      {:command command, :arguments arguments}

      :else
      {:exit-message (usage options-summary commands-summary)})))

(defn exec
  [command arguments]
  (let [exec-fn (case command
                  :sequence gnife.sequence/exec
                  :sam gnife.sam/exec
                  :vcf gnife.vcf/exec
                  :variant gnife.variant/exec
                  :hgvs gnife.hgvs/exec)]
    (exec-fn arguments)))

(defn -main
  [& args]
  (let [{:keys [command arguments exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (let [{:keys [status message]} (exec command arguments)]
          (shutdown-agents)
          (if status
            (exit status message)
            (exit 0 nil)))
        (catch Exception e
          (exit 1 (.getMessage e)))))))

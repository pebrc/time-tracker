(ns time-tracker.core
  (require [time-tracker.tracker :as t]
           [time-tracker.config :as c]
           [time-tracker.mac :as mac]
           [clojure.tools.cli :refer [parse-opts]]
           [clojure.tools.logging :as log]
           [clojure.string :as string]
           [clojure.java.io :as io])
  (:gen-class))


(def cli-options
  [["-c" "--config filename" "config file"
    :validate [#(.exists (io/as-file %)) "must be an existing file"]]
   ["-h" "--help"]])


(defn usage [options-summary]
  (->> ["Time-tracker. Tracks your time based on your usage of your computer"
        ""
        "Usage: program-name [options] "
        ""
        "Options:"
        options-summary]
              (string/join \newline)))


(defn error-msg [errors]
  (str "Could not parse options:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not (.exists (io/as-file (:config options)))) (exit 1 (error-msg ["No a valid file" (usage summary)]))
      errors (exit 1 (error-msg errors)))
    (log/debug options)
    (log/debug arguments)
    (t/track (c/conf (:config options)))))

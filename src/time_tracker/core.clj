(ns time-tracker.core
  (require [time-tracker.tracker :as t]
           [time-tracker.config :as c]
           [time-tracker.mac :as mac]
           [clojure.tools.cli :refer [parse-opts]]
           [clojure.tools.logging :as log]
           [time-tracker.log :as logger]
           [clojure.string :as string]
           [clojure.java.io :as io])
  (:gen-class))

(defn config-exists [f]
  (and (not (nil? f)) (.exists (io/as-file f))))

(def cli-options
  [["-c" "--config filename" "config file"
    :default "config.edn"
    :validate [config-exists "must be an existing file"]]
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
      errors (exit 1 (error-msg errors))
      (not (config-exists (:config options))) (exit 1 (usage summary)))
    (logger/init)
    (log/debug options)
    (log/debug arguments)
    (t/track (c/conf (:config options)))))

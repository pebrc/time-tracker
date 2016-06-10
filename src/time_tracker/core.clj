(ns time-tracker.core
  (require [time-tracker.tracker :as t]
           [time-tracker.config :as c]
           [time-tracker.mac :as mac])
  (:gen-class))



;;(t/track (c/conf))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(ns time-tracker.core
  (require [time-tracker.tracker :as t]
           [time-tracker.config :as c])
  (:gen-class))



(t/track (c/conf))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

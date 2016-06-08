(ns time-tracker.time
  (require [java-time :as t]))

(defn zoned-date-time [date tz]
  (-> (t/instant date)
      (t/zoned-date-time (t/zone-id tz))))

(defn date [{:keys [date tz]}]
  (-> (zoned-date-time date tz)
      (t/truncate-to :days))) 

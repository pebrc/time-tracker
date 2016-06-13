(ns time-tracker.tracker
  (require [time-tracker.store :as s]
           [time-tracker.nettime :as nettime]
           [time-tracker.time :refer :all]
           [clojure.spec :as spec]
           [java-time :as t]
           [clojure.tools.logging :as log]))

(defmulti time-data (fn [env cfg params] env))

(defmethod time-data :default [env c params]
  {:error (str  "no time-date implementation for " env)})


(defn to-params [stored]
  (let [{:keys [::s/from ::s/tz] :as prev}  (last (sort-by ::s/from stored))
        epoch (t/zoned-date-time 1970 1)
        prev-to   (if from
                    (zoned-date-time from tz)
                    epoch)
        next  (-> prev-to
                  (t/truncate-to :days)
                  (t/plus (t/days 1)))
        yesterday (t/minus (date (t/instant) tz) (t/days 1))
        iv (if-not (t/after? yesterday next)
             [epoch (t/plus epoch (t/millis 1))]
             [next yesterday])        
        _ (println iv)
        
        _ (log/info (str "Collecting between " iv))]
    {:interval  (t/interval (first iv) (second iv))}))

(defn validate [msg data]
  (if-let [errors (spec/explain-data ::s/store data)]
    (throw (ex-info msg errors))
    data))

(defn track [conf]
  (let [stored  (s/read-store conf)]
    (->> (to-params stored)
         (time-data (System/getProperty "os.name") conf )
         (validate "invalid data collected")
         (s/merge-stores (filter #(= :collected (::s/status %)) stored));;try to track previously collected data
         (validate "merged date invalid")
         (nettime/track conf);;this could be pluggable as well
         (s/merge-stores stored)
         (s/write-store conf))))

;(time-data "Mac OS X" {:tz "Europe/Vienna"} { :interval (t/interval (t/zoned-date-time 2016 06 03) (t/zoned-date-time 2016 06 06))})


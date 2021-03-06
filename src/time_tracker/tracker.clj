(ns time-tracker.tracker
  (require [time-tracker.store :as s]
           [time-tracker.nettime :as nettime]
           [time-tracker.time :refer :all]
           [clojure.spec :as spec]
           [java-time :as t]
           [clojure.tools.logging :as log]
           [clojure.string :as str]))

(defmulti time-data (fn [env cfg params] env))

(defmethod time-data :noop [env c params]
  (log/info "no automatic activity collection")
  [])

(defmethod time-data :default [env c params]
  {:error (str  "no time-date implementation for " env)})


(defn to-params [stored {:keys [tz]}]
  (let [{:keys [::s/from] :as prev}  (last stored)
        epoch (t/zoned-date-time 1970 1)
        prev-to   (if from
                    (zoned-date-time from tz)
                    epoch)
        next  (-> prev-to
                  (t/truncate-to :days)
                  (t/plus (t/days 1)))
        yesterday (date (t/instant) tz) 
        iv (if (or (= yesterday next) (t/after? yesterday next))
             [next yesterday]
             [epoch (t/plus epoch (t/millis 1))])        
        _ (log/info (str "collecting between " (str/join " and " (map (partial t/format "E yyyy-MM-dd HH:mm") iv))))]
    {:interval  (t/interval (first iv) (second iv))}))

(defn validate [msg data]
  (if-let [errors (spec/explain-data ::s/store data)]
    (throw (ex-info msg errors))
    data))

(defn collection-type [{:keys [manual-collection]}]
  (if manual-collection
    :noop
    (System/getProperty "os.name")))

(defn track
 "t time data
  s stored data
  m merge t s
  v validate m
  t track v
  m2 merge s t
  w write m2
  "
  [conf]
  (let [stored  (s/read-store conf)]
    (->> (to-params stored conf)
         (time-data (collection-type conf) conf )
         (validate "invalid data collected")
         (s/merge-stores (filter #(= :collected (::s/status %)) stored));;try to track previously collected data
         (validate "merged date invalid")
         (nettime/track conf);;this could be pluggable as well
         (s/merge-stores stored)
         (s/write-store conf))))

;(time-data "Mac OS X" {:tz "Europe/Vienna" :algo :exact} { :interval (t/interval (t/zoned-date-time 2016 06 16) (t/zoned-date-time 2016 06 18))})


(ns time-tracker.tracker
  (require [time-tracker.store :as s]
           [time-tracker.nettime :as nettime]
           [clojure.spec :as spec]
           [java-time :as t]))

(defmulti time-data (fn [env cfg params] env))

(defmethod time-data :default [env c params]
  {:error (str  "no time-date implementation for " env)})

(defn to-params [stored]
  {:interval (t/interval (t/zoned-date-time 1970 1) (t/minus (t/zoned-date-time) (t/days 1)))
   })

(defn validate [data]
  (if-let [errors (spec/explain-data ::s/store data)]
    (throw (ex-info "Invalid data collected" errors))
    data))

(defn track [conf]
  (let [stored  (s/read-store conf)]
    (->> (to-params stored)
         (time-data (System/getProperty "os.name") conf )
         (validate )
         (nettime/track conf);;this could be pluggable as well
         (s/merge-stores stored)
         (s/write-store conf))))

;(time-data "Mac OS X" {:tz "Europe/Vienna"} { :interval (t/interval (t/zoned-date-time 2016 06 03) (t/zoned-date-time 2016 06 06))})


(ns time-tracker.tracker)

(defmulti time-data (fn [env cfg params] env))

(defmethod time-data :default [env c params]
  {:error (str  "no time-date implementation for " env)})

(time-data (System/getProperty "os.name") {} {} )

;; read store
;; find time worked for date(s) where date != today
;; post to nettime
;; persist result(s)

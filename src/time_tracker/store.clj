(ns time-tracker.store
  (require [clojure.spec :as s]
           [java-time :as t]
           [time-tracker.io :as io])
  (import java.util.Date))

(s/def ::from #(instance? Date %))
(s/def ::to #(instance? Date %))
(s/def ::tz (set (t/available-zone-ids)))
(s/def ::status #{:collected :tracked :error})
(s/def ::message string?)
(s/def ::record (s/and  (s/keys :req [::from ::to ::tz ::status])
                        (fn [{:keys [::from ::to]}] < from to )))

(s/def ::store (s/* ::record))


;(s/explain-data ::store [{::from #inst "2016-06-01" ::to (Date.) ::tz "Europe/Vienna" ::status :collected}] )

(defn merge-stores [store & stores]
  store)


(defn read-store [{:keys [data-dir]}]
  (let [data  (io/read-edn data-dir)
        errors (s/explain-data data)]
    (if errors
      (throw (ex-info "Invalid data in store" errors))
      data)))

(defn write-store [{:keys [data-dir]} data]
  (io/write-edn data data-dir))



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
  (let [merged (->> (apply concat store stores)
                    (reduce #(assoc %1 (::from %2) %2) {})
                    (vals))
        _ (println merged)]
    merged))

;; (def sample [{::from #inst "2016-06-05T10:06:12+02:00", ::status :collected, ::tz "Europe/Vienna" ::to #inst "2016-06-05T17:48:26+02:00", :error "Die eingegebene Zeit überschneidet sich mit einer bereits erfassten Zeit!"} {::from #inst "2016-06-06T18:19:10+02:00", ::status :collected, ::tz "Europe/Vienna" ::to #inst "2016-06-06T19:06:10+02:00", :error "Entweder Pausenzeiten korrigieren, Checkbox automatisch Pause buchen  wegklicken oder Zeit von/bis korrigieren!"}])

;; (def sample2 [{::from #inst "2016-06-05T10:06:12+02:00", ::status :tracked, ::tz "Europe/Vienna" ::to #inst "2016-06-05T17:48:26+02:00"} {::from #inst "2016-06-06T18:19:10+02:00", ::status :collected, ::tz "Europe/Vienna" ::to #inst "2016-06-06T19:06:10+02:00", :error "Entweder Pausenzeiten korrigieren, Checkbox automatisch Pause buchen  wegklicken oder Zeit von/bis korrigieren!"} {::from #inst "2016-06-07T10:19:10+02:00", ::status :collected, ::tz "Europe/Vienna" ::to #inst "2016-06-06T19:06:10+02:00", :error "Entweder Pausenzeiten korrigieren, Checkbox automatisch Pause buchen  wegklicken oder Zeit von/bis korrigieren!"}])


;; (def merged (merge-stores sample sample2))

;; (count merged)



(def default-data-dir (str (System/getProperty "user.dir") "/.time-tracker") )
(def store-tail "tail.edn")
(defn store-file [dir]
  (str dir "/" store-tail))


(defn read-store [{:keys [data-dir] :or {data-dir default-data-dir}}]
  (let [store (store-file data-dir)
        data  (if (io/exists? store) (io/read-edn store) [])
        errors (s/explain-data ::store data)]
    (if errors
      (throw (ex-info "Invalid data in store" errors))
      data)))

(defn write-store [{:keys [data-dir] :or {data-dir default-data-dir}} data]
  (io/write-edn data (store-file data-dir)))



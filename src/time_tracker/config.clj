(ns time-tracker.config
  (require [time-tracker.io :as io]
           [clojure.spec :as s]))

;;; SPECs
(s/def ::user string?)
(s/def ::pw string?)
(def project-regex #"^\d{6}\.\d{2}$")
(s/def ::project (s/and string? #(re-matches project-regex %)) )
(s/def ::config (s/keys :req-un [::user ::pw ::project]))



(defn conf
  ([]
   (conf "config.edn"))
  ([f]
   (let [c (io/read-edn f)]
     (if (s/valid? ::config c)
       c))))

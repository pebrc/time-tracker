(ns time-tracker.config
  (require [time-tracker.io :as io]
           [clojure.spec :as s]
           [java-time :as t]))

;;; SPECs
(s/def ::user string?)
(s/def ::pw string?)
(s/def ::data-dir string?)
(s/def ::algo #{:accumulate :exact :maximize})
(s/def ::manual-collection boolean?)
(def project-regex #"^\d{6}\.\d{2}$")
(s/def ::project (s/and string? #(re-matches project-regex %)) )
(s/def ::config (s/keys :req-un [::user ::pw ::project]
                        :opt-un [:trime-tracker.store/tz
                                 ::algo ::manual-collection]))

(def defaults {:tz (.getId (t/zone-id))
               :algo :maximize
               :data-dir (str (System/getProperty "user.dir") "/.time-tracker")})

(defn conf
  ([]
   (conf "config.edn"))
  ([f]
   (let [c (io/read-edn f)]
     (if (s/valid? ::config c)
       (merge defaults c)))))

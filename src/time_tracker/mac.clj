(ns time-tracker.mac
  (require [clojure.java.shell :refer [sh]]
           [clojure.string :as s]
           [time-tracker.tracker :refer [time-data]]))

(defn raw []
  (let [res (sh "pmset" "-g" "log")]
    (if (= 0 (:exit res))
      (:out res)
      "")))

(defn parse-secs [s]
  "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%) 3286 secs"
  (if-let [s (second  (re-find #".* (\d+) secs$" s))]
    (Integer. s)
    0))


(defn parse-date [s]
  "Date formaat used by pmset: 2016-06-01 23:27:21 +0200"
  (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss Z") s))


(defn parse-date-domain [[head tail]]
  (try
    (let [dstr (subs head 0 26)
          domain (s/trim (subs head 26))]
      (assoc {} :date (parse-date dstr) :domain (keyword (s/lower-case domain)) :msg (s/trim tail)))
    (catch Exception e {:error e :context [head tail]})))


(defn assoc-secs [d]
  (let [secs (parse-secs (:msg d))]
    (conj d (when secs [:secs secs]))))

(def domains #{:wake :sleep :darkwake})

(def drop-junk
  (filter #(and (= (count %) 2) (not (s/blank? (first %))))))

(def txform
  (comp
   (map #(s/split % #"\t"))
   drop-junk
   (map parse-date-domain)
   (filter #(domains (:domain %)))
   (map assoc-secs)))


(defmethod time-data "Mac OS X" [env c params]
   (into [] txform  (->> (raw)
                        (s/split-lines))))




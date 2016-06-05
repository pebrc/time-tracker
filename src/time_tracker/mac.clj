(ns time-tracker.mac
  (require [clojure.java.shell :refer [sh]]
           [clojure.string :as s]
           [java-time :as t]
           [time-tracker.tracker :refer [time-data]]
           [time-tracker.store :as r]))

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

(def parse
  (comp
   (map #(s/split % #"\t"))
   drop-junk
   (map parse-date-domain)
   (filter #(domains (:domain %)))
   (map assoc-secs)))


(defn date [{:keys [date tz]}]
  (-> (t/instant date)
      (t/zoned-date-time (t/zone-id tz))
      (t/truncate-to :days))) 

(defn assoc-pkey [e]
  (assoc e :pkey (date e)))

(defn to-record [es]
  (reduce
   (fn [result {:keys [date secs ]}]
     (update-in result [:r/to] #(t/plus (or % (t/instant date))  (t/seconds secs))))
   {:r/from (:date (first es)) :r/status :collected :r/tz (:tz (first es)) }
   es))

(defn aggregate [{:keys [tz]} {:keys [interval] }] 
  (comp
   (filter #(= :wake (:domain %)))
   (map #(assoc % :tz tz))
   (map assoc-pkey)
   (filter #(t/contains? interval (:pkey %)))
   (partition-by :pkey)
   (map to-record)))



;; (def sample
;;  (into [] parse  (->> (raw)
;;                       (s/split-lines))) )

;(into [] (aggregate {:tz "Europe/Vienna"} { :interval (t/interval (t/zoned-date-time 2016 06 03) (t/zoned-date-time 2016 06 06))}) sample)

(defmethod time-data "Mac OS X" [env c params]
  (into []
        (comp parse
              (aggregate c params))
        (->> (raw)
             (s/split-lines))))




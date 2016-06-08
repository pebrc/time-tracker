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

(defn zoned-date-time [date tz]
  (-> (t/instant date)
      (t/zoned-date-time (t/zone-id tz))))

(defn date [{:keys [date tz]}]
  (-> (zoned-date-time date tz)
      (t/truncate-to :days))) 

(defn assoc-pkey [e]
  (assoc e :pkey (date e)))

(defn update-vals [m kfs]
  (reduce (fn [acc [k f]] (update-in [k] f)) m))


(defn to-record [es]
  "Ignores activity across day boundary e.g.
    wake -- midnight --- sleep"
  (let [neutral-el (fn [{:keys [date tz]}] {:state :unknown
                                            ::r/status :collected
                                            ::r/tz tz})
        plus-time-between  (fn [a b] (t/plus a (t/millis (t/time-between a b :millis))))]
    (reduce
     (fn [{state :state last-from :last-from from ::r/from to ::r/to :as result} {:keys [date domain tz]}]
       (condp = [state domain]
         [:unknown :sleep] :>> (fn [_] (assoc result :state :sleep))
         [:wake :sleep] :>> (fn [_] (assoc result ::r/to (if-not to (zoned-date-time date tz) (t/plus to (t/millis (t/time-between last-from (zoned-date-time date tz) :millis)))) :state :sleep))
         [:unknown :wake] :>> (fn [_] (assoc result ::r/from (zoned-date-time date tz) :last-from (zoned-date-time date tz) :state :wake))
         [:sleep :wake] :>> (fn [_] (assoc result ::r/from (or from (zoned-date-time date tz)) :last-from (zoned-date-time date tz) :state :wake))
         result))
     (neutral-el (first es))
     es)))


(defn aggregate [{:keys [tz]} {:keys [interval] }] 
  (comp
   (map #(assoc % :tz tz))
   (map assoc-pkey)
   (filter #(t/contains? interval (:pkey %)))
   (partition-by :pkey)
   (map to-record)))



;; (def sample
;;  (into [] parse  (->> (raw)
;;                       (s/split-lines))) )


;(into [] (aggregate {:tz "Europe/Vienna"} { :interval (t/interval (t/zoned-date-time 2016 6 3) (t/zoned-date-time 2016 6 9)) }) sample)


(defmethod time-data "Mac OS X" [env c params]
  (into []
        (comp parse
              (aggregate c params))
        (->> (raw)
             (s/split-lines))))




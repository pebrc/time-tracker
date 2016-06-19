(ns time-tracker.mac
  (require [clojure.java.shell :refer [sh]]
           [clojure.string :as s]
           [clojure.tools.logging :as log]
           [java-time :as t]
           [time-tracker.time :refer :all]
           [time-tracker.tracker :refer [time-data]]
           [time-tracker.store :as r])
  (import java.util.Date))

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


(defn assoc-pkey [e]
  (assoc e :pkey (date e)))

(defn update-vals [m kfs]
  (reduce (fn [acc [k f]] (update-in [k] f)) m))

(defn accumulate [es]
    "Ignores activity across day boundary e.g.
    wake -- midnight --- sleep"
  (let [neutral-el (fn [{:keys [tz]}] {:state :unknown
                                            ::r/status :collected
                                            ::r/tz tz})
        +-diff (fn [a b c] (Date. (+ (.getTime a) (- (.getTime c) (.getTime b)))) )]
    (-> (reduce
         (fn [{state :state last-from :last-from from ::r/from to ::r/to :as result} {:keys [date domain tz]}]
           (condp = [state domain]
             [:unknown :sleep] :>> (fn [_] (assoc result :state :sleep))
             [:wake :sleep] :>> (fn [_] (assoc result ::r/to (if-not to date (+-diff to last-from date) ) :state :sleep))
             [:unknown :wake] :>> (fn [_] (assoc result ::r/from date  :last-from date :state :wake))
             [:sleep :wake] :>> (fn [_] (assoc result ::r/from (or from date) :last-from date  :state :wake))
             result))
         (neutral-el (first es))
         es)
        (dissoc :state :last-from)
        (vector))))


(defn exact [es]
  (let [neutral-el (fn [{:keys [tz]}] [{:state :unknown
                                         ::r/status :collected
                                        ::r/tz tz} []])]
    (->> (reduce
          (fn [[{state :state  from ::r/from  :as cur} acc] {:keys [date domain tz]}]
            (condp = [state domain]
              [:unknown :sleep] :>> (fn [_] [(assoc cur :state :sleep) acc])
              [:wake :sleep] :>> (fn [_] [(assoc cur :state :sleep) (conj acc (assoc cur ::r/to date))])
              [:unknown :wake] :>> (fn [_] [(assoc cur ::r/from date  :state :wake) acc])
              [:sleep :wake] :>> (fn [_] [(assoc cur ::r/from date :state :wake) acc])
              [cur acc]))
          (neutral-el (first es))
          es)
         (second))))


(defn maximise [es]
  (let [neutral-el (fn [{:keys [date tz]}] {:state :unknown
                                            ::r/status :collected
                                            ::r/tz tz})]
    (-> (reduce
         (fn [{state :state  from ::r/from  :as result} {:keys [date domain tz]}]
           (condp = [state domain]
             [:unknown :sleep] :>> (fn [_] (assoc result :state :sleep))
             [:wake :sleep] :>> (fn [_] (assoc result ::r/to date :state :sleep))
             [:unknown :wake] :>> (fn [_] (assoc result ::r/from date  :state :wake))
             [:sleep :wake] :>> (fn [_] (assoc result ::r/from (or from date)  :state :wake))
             result))
         (neutral-el (first es))
         es)
        (dissoc :state )
        (vector))))

(defn to-record [algo]
  (case algo
    :accumulate accumulate
    :exact exact
    maximise ))


(defn aggregate [{:keys [tz algo]} {:keys [interval] }]
  (comp
   (map #(assoc % :tz tz))
   (map assoc-pkey)
   (filter #(t/contains? interval (:pkey %)))
   (partition-by :pkey)
   (mapcat (to-record algo))
   (filter #(not (nil? (::r/from %))))))

;; (def sample
;;  (into [] parse  (->> (raw)
;;                       (s/split-lines))) )

(defmethod time-data "Mac OS X" [env c params]
  (let [res  (into []
                   (comp parse
                         (aggregate c params))
                   (->> (raw)
                        (s/split-lines)))
        _ (log/debug res)]
    res))




; (time-data "Mac OS X" {:tz "Europe/Vienna" :algo :exact} { :interval (t/interval (t/zoned-date-time 2016 06 16) (t/zoned-date-time 2016 06 18))})q

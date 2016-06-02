(ns time-tracker.mac
  (require [clojure.java.shell :refer [sh]]
           [clojure.string :as s]))

(defn raw []
  (let [res (sh "pmset" "-g" "log")]
    (if (= 0 (:exit res))
      (:out res)
      "")))

(defn parse-secs [s]
  "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%) 3286 secs"
  (second  (re-find #".* (\d+) secs$" s)))

;;(parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%) 3286 secs")

;;(parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%)")

(defn parse-date [s]
  "Date formaat used by pmset: 2016-06-01 23:27:21 +0200"
  (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss Z") s))


(defn parse-date-domain [[head tail]]
  (try
    (let [dstr (subs head 0 26)
          domain (s/trim (subs head 26))]
      (assoc {} :date (parse-date dstr) :domain (keyword domain) :msg (s/trim tail)))
    (catch Exception e {:error e :context [head tail]})))


(parse-date-domain ["2016-06-01 21:05:58 +0200 Assertions          " ""])

(parse-date "2016-06-01 23:27:21x +0200 sfsdfsdfsafjskfjs")

(->> (raw)
     (s/split-lines)
     (map #(s/split % #"\t"))
     (filter #(and (= (count %) 2) (not (s/blank? (first %)))))
     (map parse-date-domain)
     (filter #(= :Wake (:domain %)))
;;     (filter :error)
     )




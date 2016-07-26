(ns time-tracker.mac-test
  (:require [clojure.test :refer :all]
            [time-tracker.mac :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [java-time :as t]))

(deftest can-parse-secs
  (is (= 0 (parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%)")))
  (is (= 3286 (parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%) 3286 secs"))))


(deftest can-parse-dates
  (is (= {:date #inst "2016-06-01T19:05:58.000-00:00", :domain :assertions, :msg ""} (parse-date-domain ["2016-06-01 21:05:58 +0200 Assertions          " ""]))))


(deftest can-parse-sparse-data
  (let [bogus (->> "bogus.txt"
                   io/resource
                   slurp
                   str/split-lines)
        parsed (into [] parse bogus)
        params {:interval (t/interval (t/zoned-date-time 2016 7 24) (t/zoned-date-time 2016 7 25))}
        config {:tz "Europe/Vienna"
                :algo :maximise}]
    (is (=
         (into [] (aggregate config params) parsed)
         [#:time-tracker.store {:status :collected, :tz "Europe/Vienna", :from #inst "2016-07-24T17:33:35.000-00:00" :to #inst "2016-07-24T21:59:59.000-00:00"}]))))

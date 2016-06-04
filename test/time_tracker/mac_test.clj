(ns time-tracker.mac-test
  (:require [clojure.test :refer :all]
            [time-tracker.mac :refer :all]))

(deftest can-parse-secs
  (is (= 0 (parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%)")))
  (is (= 3286 (parse-secs "Wake from Standby [CDNVA] due to EC.LidOpen/Lid Open: Using BATT (Charge:100%) 3286 secs"))))


(deftest can-parse-dates
  (is (= {:date #inst "2016-06-01T19:05:58.000-00:00", :domain :Assertions, :msg ""} (parse-date-domain ["2016-06-01 21:05:58 +0200 Assertions          " ""]))))

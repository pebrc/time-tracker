(ns time-tracker.nettime
  (require [clj-webdriver.taxi :refer :all]
           [java-time :as t]
           [clojure.tools.logging :as log]
           [time-tracker.store :as r]
           [time-tracker.time :refer :all]))

(defn input [name]
  (str "input[name='" name "']"))

(defn login [{:keys [user pw url] :or {url "https://nettime.brainforce.com/"}}]
  (to url)
  (switch-to-frame "frame[name='workframe']")
  (input-text (input "F_UNr") user)
  (select-by-text "select[name='F_MandantenNr'" "SD")
  (input-text (input "F_Passwort") pw)
  (click (input "F_Login"))
  (implicit-wait 3000)
  (wait-until #(exists? (input "F_UId"))))

(defn logoff []
  (switch-to-default)
  (switch-to-frame "frame[name='menuframe']")
  (click "a[href*='nt_abmelden.asp']"))


(def error-q "font[color='red']" )

(defn error? []
  (exists? error-q))

(defn collect-error [r]
  (if (error?)
    (assoc r ::r/message (text error-q) ::r/status :error)
    (assoc r ::r/status :tracked)))

(defn fill [e v]
  (-> e
      (clear)
      (input-text v)))

(defn de-select [e]
  (if (selected? (element e))
    (click e)))

(defn record-entry [{:keys [project]} {:keys [::r/from ::r/to ::r/tz] :as r}]
  (let [from-z (zoned-date-time from tz)
        to-z (zoned-date-time to tz)]
    (click (input "F_Reset"))
    (wait-until #(not (exists? (input "F_KAId"))))
    (fill (input "F_VonDat") (t/format "dd.MM.yyyy" from-z))
    (fill (input "F_VonZeit") (t/format "HH:mm" from-z))
    (fill (input "F_BisZeit") (t/format "HH:mm" to-z))
    (input-text (input "F_PId") project)
    (click (input "F_Aktual"))
    (wait-until #(exists? (input "F_KAId")))
    (de-select "input[type='checkbox'][name='F_Pausebuchen']")
    (fill "textarea[name='F_Text']" "via time-tracker")
    (click (input "F_Speichern"))
    (collect-error r)))





;;(def sample [{:date #inst "2016-06-01" :from "09:00" :to "17:00"}])

(defn do-on-nettime [conf op]
  (do
    (set-driver! {:browser :firefox})
    (login conf)
    (let [result (op)]
      (logoff)
      (quit)
      result)))

(defn track [conf rs]
  (let [track (partial record-entry conf)]
    (do-on-nettime conf #(doall (map track rs))) ))


;;  (track conf sample)



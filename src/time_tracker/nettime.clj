(ns time-tracker.nettime
  (require [clj-webdriver.taxi :refer :all]
           [java-time :as t]))

(defn input [name]
  (str "input[name='" name "']"))

(defn to-local-date-time [i]
  (t/local-date-time (t/instant i) "UTC"))

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

(defn collect-error []
  (if (error?)
    {:error  (text error-q)}))


(defn record-entry [{:keys [project]} e]
  (-> (input "F_VonDat")
      (clear)
      (input-text  (t/format "dd.MM.yyyy" (to-local-date-time (:date e)))))
  (input-text (input "F_VonZeit") (:from e))
  (input-text (input "F_BisZeit") (:to e))
  (input-text (input "F_PId") project)
  (input-text "textarea[name='F_Text']" "via time-tracker")
  (click (input "F_Aktual"))
  (wait-until #(exists? (input "F_KAId")))
  (click (input "F_Speichern"))
  (collect-error))

;;

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
    (do-on-nettime conf #(doall (map track rs)))))


;;  (track conf sample)



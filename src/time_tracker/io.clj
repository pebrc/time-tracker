(ns time-tracker.io
  (require [clojure.edn :as edn]
           [clojure.java.io :as io]))

(defn exists? [filename]
  (.exists (io/file filename)))

(defn read-edn [filename]
  (edn/read-string (slurp filename)))

(defn write-edn [data filename]
  (let [file (io/file filename)]
    (when-not (.exists file)
      (io/make-parents file))
    (spit file (with-out-str (pr data)))))

(ns time-tracker.io
  (require [clojure.edn :as edn]
           [clojure.java.io :as io]
           [clojure.pprint :as p]))

(defn exists? [filename]
  (.exists (io/file filename)))

(defn read-edn [filename]
  (edn/read-string (slurp filename)))

(defn write-edn [data filename pretty]
  (let [file (io/file filename)]
    (when-not (.exists file)
      (io/make-parents file))
    (spit file (with-out-str (if pretty (p/pprint data) (pr data))))))

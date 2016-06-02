(defproject time-tracker "0.1.0-SNAPSHOT"
  :description "tracks the time you spent on your Mac"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha4"]]
  :main ^:skip-aot time-tracker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

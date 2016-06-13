(defproject time-tracker "0.1.0-SNAPSHOT"
  :description "tracks the time you spent on your Mac"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha4"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-webdriver "0.7.2"]
                 [org.seleniumhq.selenium/selenium-java "2.52.0"]
                 [clojure.java-time "0.2.0"]
                 [org.threeten/threeten-extra "0.9"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]]
  :main ^:skip-aot time-tracker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(defproject hitting-set "0.8.0-SNAPSHOT"
            :description "Calculate minimal hitting sets"
            :url "https://github.com/bdesham/hitting-set"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"
                      :distribution :repo
                      :comments "Same as Clojure"}
            :min-lein-version "2.0.0"
            :dependencies [[org.clojure/clojure "1.4.0"]]
            :profiles {:dev {:dependencies [[criterium "0.3.0"]]}})

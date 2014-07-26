(defproject reca "0.1.0"
  :description "Reca is a wrapper for Mahout recommendation algorithms."
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url ""}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.mahout/mahout-core "0.9"]
                 [org.apache.mahout/mahout-integration "0.9"]
                 [org.clojure/java.jdbc "0.3.4"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [expectations "2.0.6"]]
  :plugins [[lein-expectations "0.0.7"]
            [lein-autoexpect "1.0"]
            [codox "0.8.10"]])

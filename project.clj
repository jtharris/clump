(defproject clump "0.1.0-SNAPSHOT"
  :description "A really hip Database <-> CSV importer/exporter"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.csv "0.1.2"]
                 [korma "0.3.0-RC6"]
                 ;; Just starting w/ SQLite for now I guess...
                 [org.xerial/sqlite-jdbc "3.7.2"]]
  :main clump.core)

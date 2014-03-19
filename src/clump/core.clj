(ns clump.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]))

; Temp!
(def target-db { :classname "org.sqlite.JDBC"
                 :subprotocol "sqlite"
                 :subname "resources/target.db"})

(defn load-file
  [file]
  (with-open [f (io/reader file)]
    (doall
     (csv/read-csv f))))

(defn import-data
  [file table-name]

  ; Also clean out any rows that are there
  (j/delete! target-db table-name [])

  (let [file-data (load-file file)]
    (apply j/insert! target-db table-name (first file-data) (rest file-data))))

(defn table-name
  [file]
  (let [file-name (.getName file)]
    (.substring file-name 0 (- (count file-name) 4))))

(defn import-csvs
  [import-dir]

  (def directory (clojure.java.io/file import-dir))
  (def files
    (filter #(.endsWith (.getName %) ".csv") (file-seq directory)))

  (doall (map #(import-data % (table-name %)) files)))


(defn -main
  [& args]
  (import-csvs (first args)))




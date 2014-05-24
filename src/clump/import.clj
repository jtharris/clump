(ns clump.import
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clump.db :as db]))

(defn load-file
  [file]
  (with-open [f (io/reader file)]
    (doall
     (csv/read-csv f))))

(defn import-data
  [file table-name]
  (j/delete! db/target-db table-name [])

  (let [file-data (load-file file)]
    (apply j/insert! db/target-db table-name (first file-data) (rest file-data))))

(defn table-name-from-file
  [file]
  (let [file-name (.getName file)]
    (.substring file-name 0 (- (count file-name) 4))))

(defn import-csvs
  [import-dir]
  (let [files (filter #(.endsWith (.getName %) ".csv") (file-seq (io/file import-dir)))]
    (doall (map #(import-data % (table-name-from-file %)) files))))

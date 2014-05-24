(ns clump.export
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clojure.string :as s]
            [clump.db :as db]))

(defn file-name
  [table-map]
  (if (s/blank? (:table_schem table-map))
     (str (:table_name table-map) ".csv")
     (str (:table_schem table-map) "." (:table_name table-map) ".csv")))

(defn table-name-from-map
  [table-map]
  (if (s/blank? (:table_schem table-map))
    ;TODO:  Use db agnostic escaping here - this is MS SQL specific
    (str "[" (:table_name table-map) "]")
    (str "[" (:table_schem table-map) "].[" (:table_name table-map) "]")))

(defn export-data
  [export-dir table-map]
  ; TODO:  Pull this into db ns
  (let [table-data (j/query
                      db/target-db (str "select * from " (table-name-from-map table-map))
                      :as-arrays? true)]
    (println (str "Exporting:  " (file-name table-map)))

    (with-open [f (io/writer (io/file export-dir (file-name table-map)))]
      (csv/write-csv f (cons (map name (first table-data)) (rest table-data))))))

(defn export-csvs
  [export-dir]
  (doall
    (pmap (partial export-data export-dir) db/tables-list)))

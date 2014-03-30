(ns clump.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clojure.edn :as edn]
            [clojure.string :as s]))

(def config (edn/read-string (slurp "resources/config.edn")))
(def target-db (:jdbc-connection config))

(defn load-file
  [file]
  (with-open [f (io/reader file)]
    (doall
     (csv/read-csv f))))

(defn import-data
  [file table-name]
  (j/delete! target-db table-name [])

  (let [file-data (load-file file)]
    (apply j/insert! target-db table-name (first file-data) (rest file-data))))

(defn table-name-from-file
  [file]
  (let [file-name (.getName file)]
    (.substring file-name 0 (- (count file-name) 4))))

(defn import-csvs
  [import-dir]
  (let [files (filter #(.endsWith (.getName %) ".csv") (file-seq (io/file import-dir)))]
    (doall (map #(import-data % (table-name-from-file %)) files))))


(def tables-list
  (j/with-db-connection [con target-db]
    (doall
      (j/result-set-seq
        (-> (:connection con) .getMetaData (.getTables nil nil nil (into-array String ["TABLE"])))))))

(defn table-name-from-map
  [table-map]
  (if (s/blank? (:table-schem table-map))
     (str "[" (:table_name table-map) "]")
     (str "[" (:table_schem table-map) "].[" (:table_name table-map) "]")))

(defn file-name
  [table-map]
  (if (s/blank? (:table-schem table-map))
     (str (:table_name table-map) ".csv")
     (str (:table_schem table-map) "." (:table_name table-map) ".csv")))

(defn export-data
  [export-dir table-map]
  (let [table-data (j/query
                      target-db (str "select * from " (table-name-from-map table-map))
                      :as-arrays? true)]
    (println (str "Exporting:  " (file-name table-map)))

    (with-open [f (io/writer (io/file export-dir (file-name table-map)))]
      (csv/write-csv f (cons (map name (first table-data)) (rest table-data))))))

(defn export-csvs
  [export-dir]
  (doall
    (pmap (partial export-data export-dir) tables-list)))

(defn -main
  [& args]

  (let [action (first args)]
    (cond
     (= action "import") (import-csvs (io/resource (str "../" (:input-dir config))))
     (= action "export") (export-csvs (io/resource (str "../" (:output-dir config))))
     :else (println "Please specify 'import' or 'export'"))))




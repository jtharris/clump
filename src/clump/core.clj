(ns clump.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clojure.edn :as edn]))

(def config (edn/read-string (slurp "resources/config.edn")))
(def target-db (:jdbc-connection config))

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

  (let [files (filter #(.endsWith (.getName %) ".csv") (file-seq (io/file import-dir)))]
    (doall (map #(import-data % (table-name %)) files))))


(def tables-list
  (j/with-db-connection [con target-db]
    (doall
      (j/result-set-seq
        (-> (:connection con) .getMetaData (.getTables nil nil nil nil))))))

(defn export-data
  [table-map]
  (let [table-name (:table_name table-map)]
    (println (str "Exporting:  " table-name))))

(defn export-csvs
  [export-dir]
  (doall (map export-data tables-list)))

(defn -main
  [& args]

  (let [action (first args)]
    (cond
      (= action "import") (import-csvs (io/resource (str "../" (:input-dir config))))
      (= action "export") (export-csvs (io/resource (str "../" (:output-dir config))))
      :else (println "Please specify 'import' or 'export'"))))




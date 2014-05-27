(ns clump.db
  (:require [clojure.java.jdbc :as j]
            [clojure.edn :as edn]))

(def target-db (:jdbc-connection (edn/read-string (slurp "resources/config.edn"))))

(def tables-list
  (j/with-db-metadata [md target-db]
    (j/metadata-result (.getTables md nil nil nil (into-array String ["TABLE"])))))

(defn find-fk-tables
  [table]
  (map #(into {} {:table_name (:fktable_name %), :table_schem (:fktable_schem %)})
    (j/with-db-metadata [md target-db]
      (j/metadata-result (.getExportedKeys md nil nil (:table_name table))))))

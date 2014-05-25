(ns clump.db
  (:require [clojure.java.jdbc :as j]
            [clojure.edn :as edn]))

(def target-db (:jdbc-connection (edn/read-string (slurp "resources/config.edn"))))

(defn find-dependent-tables
  [table-name]
  (j/with-db-connection [con target-db]
    (map #(into {} {:table_name (:pktable_name %), :table_schem (:pktable_schem %)})
      (doall
        (j/result-set-seq
          (-> (:connection con) .getMetaData (.getImportedKeys nil nil table-name)))))))

(def tables-list
  (j/with-db-connection [con target-db]
    ; TODO:  Look at refactoring this using j/with-db-metadata
    (doall
      (j/result-set-seq
        (-> (:connection con) .getMetaData (.getTables nil nil nil (into-array String ["TABLE"])))))))

(ns clump.db
  (:require [clojure.java.jdbc :as j]
            [clojure.edn :as edn]))

(def target-db (:jdbc-connection (edn/read-string (slurp "resources/config.edn"))))

(def tables-list
  (j/with-db-connection [con target-db]
    ; TODO:  Look at refactoring this using j/with-db-metadata
    (doall
      (j/result-set-seq
        (-> (:connection con) .getMetaData (.getTables nil nil nil (into-array String ["TABLE"])))))))

(defn find-dependent-tables
  [table]
  (j/with-db-connection [con target-db]
    (map #(into {} {:table_name (:pktable_name %), :table_schem (:pktable_schem %)})
      (doall
        (j/result-set-seq
          (-> (:connection con) .getMetaData (.getImportedKeys nil nil (:table_name table))))))))

(defn ordered-tables-list
  [current tables]
  (let [deps (zipmap tables (map find-dependent-tables tables))]
    (map #((if (empty? %2) (conj current %1) (ordered-tables-list current %2))) (seq deps))))

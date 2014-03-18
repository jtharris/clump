(ns clump.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [korma.db :refer :all]
            [korma.core :refer :all]))

(defn load-file
  [file]
  (with-open [f (io/reader file)]
    (doall
     (csv/read-csv f))))

(defn import-data
  [file table-name]
  (create-entity table-name)

  ; Also clean out any rows that are there
  (delete table-name)

  (let [file-data (load-file file)]
    ; This shit should work according to the docs but the list version of 'values' is all fubar for some reason
    ;(insert table-name
    ;   (values (map (partial zipmap (first file-data)) (rest file-data)))))

    ; So this is the alternative
    (doseq [entity (map (partial zipmap (first file-data)) (rest file-data))]
      (insert table-name
         (values entity)))))

(defn table-name
  [file]
  (let [file-name (.getName file)]
    (.substring file-name 0 (- (count file-name) 4))))


(defn import-csvs
  [import-dir]

  ; Set up the database table
  ; TODO:  Configure this externally
  (defdb target-db
    (sqlite3 {:db "resources/target.db"}))

  (def directory (clojure.java.io/file import-dir))
  (def files
    (filter #(.endsWith (.getName %) ".csv") (file-seq directory)))


  (doall (map #(import-data % (table-name %)) files)))



(defn -main
  [& args]
  (import-csvs (first args)))




(ns clump.core)

(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io]
         'korma.db)

(use 'korma.db 'korma.core)

(defn load-file[file]
  (with-open [f (io/reader file)]
    (doall
     (csv/read-csv f))))

(defn import-csvs
  [import-dir]

  ; Set up the database table
  (defdb target-db
    (sqlite3 {:db "resources/target.db"}))

  (def directory (clojure.java.io/file import-dir))
  (def files
    (filter #(.endsWith (.getName %) ".csv") (file-seq directory)))

  ; TODO:  parallelize this?
  (doseq [file files]
    ; Define the db entity with korma
    (def filename (.getName file))
    (def table-name (.substring filename 0 (- (count filename) 4)))
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
           (values entity))))))



(defn -main
  [& args]
  (import-csvs (first args)))




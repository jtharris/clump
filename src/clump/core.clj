(ns clump.core)

(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io]
         'korma.db)

(use 'korma.db 'korma.core)

(defn -main
  [& args]

  ; Set up the database table
  (defdb target-db
    (sqlite3 {:db "resources/target.db"}))

  (defentity users)

  (let [file-data
    (with-open [file (io/reader (first args))]
      (doall
        (csv/read-csv file)))]

    ; This shit should work according to the docs but the list version of 'values' is all fubar for some reason
    ;(insert users
    ;   (values (map (partial zipmap (first file-data)) (rest file-data)))))

    ; So this is the alternative
    (doseq [entity (map (partial zipmap (first file-data)) (rest file-data))]
      (insert users
         (values entity))))

)




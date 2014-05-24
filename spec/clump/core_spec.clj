(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clump.db :as db]
            [clump.export :as ce]
            [clump.import :as ci]
            [clojure.java.jdbc :as j]
            [clojure.java.io :as io]))

(defn setup-test-db
  []
  (j/db-do-commands db/target-db "PRAGMA foreign_keys = OFF;")
  (doseq [table-name (map :table_name db/tables-list)]
    (j/db-do-commands db/target-db (j/drop-table-ddl table-name)))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :users
                        [:id :integer "PRIMARY KEY"]
                        [:name :text]
                        [:email :text]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :humans
                        [:id :integer "PRIMARY KEY"]
                        [:user_id :integer "REFERENCES users"]
                        [:age :integer]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :pets
                        [:id :integer "PRIMARY KEY"]
                        [:species :text]
                        [:name :text]
                        [:owner_id :integer "REFERENCES humans"]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :cars
                        [:id :integer "PRIMARY KEY"]
                        [:year :integer]
                        [:make :text]
                        [:model :text]
                        [:owner_id :integer "REFERENCES humans"])))

  (j/db-do-commands db/target-db "PRAGMA foreign_keys = ON;")


(describe "Import/Export parity"
  (before-all
    (setup-test-db)
    (ci/import-csvs
      (io/resource "../resources/input"))

    (ce/export-csvs
      (io/resource "../resources/output")))

  (for [file ["cars.csv" "users.csv" "humans.csv"]]
    (it (str "should export " file " correctly")
      (should= (slurp (io/resource (str "resources/input/" file)))
               (slurp (io/resource (str "resources/output/" file)))))))

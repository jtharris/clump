(ns clump.fixtures
  (:require [clump.db :as db]
            [clojure.java.jdbc :as j]))

(defn setup-test-db
  []
  (j/db-do-commands db/target-db "PRAGMA foreign_keys = OFF;")
  (doseq [table-name (map :table_name db/tables-list)]
    (j/db-do-commands db/target-db
       (j/drop-table-ddl table-name)))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :users
                        [:id :integer "PRIMARY KEY"]
                        [:name :text]
                        [:email :text]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :humans
                        [:id :integer "PRIMARY KEY"]
                        [:user_id :integer "REFERENCES users(id)"]
                        [:age :integer]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :pets
                        [:id :integer "PRIMARY KEY"]
                        [:owner_id :integer "REFERENCES humans(id)"]
                        [:species :text]
                        [:name :text]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :colors
                        [:id :integer "PRIMARY KEY"]
                        [:name :text]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :cars
                        [:id :integer "PRIMARY KEY"]
                        [:year :integer]
                        [:make :text]
                        [:model :text]
                        [:owner_id :integer "REFERENCES humans(id)"]))

  (j/db-do-commands db/target-db "PRAGMA foreign_keys = ON;"))

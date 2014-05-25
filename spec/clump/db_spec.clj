(ns clump.db-spec
  (:require [speclj.core :refer :all]
            [clump.db :as db]
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
                        [:user_id :integer "REFERENCES users(id)"]
                        [:age :integer]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :pets
                        [:id :integer "PRIMARY KEY"]
                        [:species :text]
                        [:name :text]
                        [:owner_id :integer "REFERENCES humans(id)"]))

  (j/db-do-commands db/target-db
    (j/create-table-ddl :cars
                        [:id :integer "PRIMARY KEY"]
                        [:year :integer]
                        [:make :text]
                        [:model :text]
                        [:owner_id :integer "REFERENCES humans(id)"]))

  (j/db-do-commands db/target-db "PRAGMA foreign_keys = ON;"))

(describe "tables-list"
  (before-all setup-test-db)

  (it "should fetch the correct table names"
    (let [table-names (map :table_name db/tables-list)]
      (should= ["users" "humans" "pets" "cars"] table-names))))


(describe "find-dependent-tables"
  (before-all setup-test-db)

  (it "should find no dependent tables for users table"
    (should= [] (db/find-dependent-tables "users")))

  (it "should find dependent tables for humans table"
    (should= [{:table_name "users", :table_schem nil}] (db/find-dependent-tables "humans")))

  (it "should find dependent tables for pets table"
    (should= [{:table_name "humans", :table_schem nil}] (db/find-dependent-tables "pets"))))

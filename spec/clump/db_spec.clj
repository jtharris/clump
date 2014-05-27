(ns clump.db-spec
  (:require [speclj.core :refer :all]
            [clump.db :as db]
            [clump.fixtures :as cf]
            [clojure.java.jdbc :as j]
            [clojure.java.io :as io]))

(describe "tables-list"
  (before cf/setup-test-db)

  (it "should fetch the correct table names"
    (let [table-names (map :table_name db/tables-list)]
      (should= ["cars" "colors" "humans" "pets" "users"] table-names))))


(describe "find-fk-tables"
  (before cf/setup-test-db)

  (it "should find fk tables for users table"
    (should= [{:table_name "humans" :table_schem nil}] (db/find-fk-tables {:table_name "users"})))

  (it "should find fk tables for humans table"
    (should= [{:table_name "cars" :table_schem nil} {:table_name "pets" :table_schem nil}]
             (db/find-fk-tables {:table_name "humans"})))

  (it "should find no fk tables for cars table"
    (should= [] (db/find-fk-tables {:table_name "cars"})))

  (it "should find no fk tables for pets table"
    (should= [] (db/find-fk-tables {:table_name "pets"})))

  (it "should find no fk tables for colors table"
    (should= [] (db/find-fk-tables {:table_name "colors"}))))

#_(describe "ordered-tables-list"
  (before cf/setup-test-db)

  (it "should find the correct table order"
    (let [table-names (map :table_name (db/ordered-tables-list))]
      (should= ["users" "colors" "humans" "pets" "cars"] table-names))))

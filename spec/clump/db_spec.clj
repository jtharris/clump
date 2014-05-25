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
      (should= ["users" "humans" "pets" "colors" "cars"] table-names))))


(describe "find-dependent-tables"
  (before cf/setup-test-db)

  (it "should find no dependent tables for users table"
    (should= [] (db/find-dependent-tables {:table_name "users"})))

  (it "should find dependent tables for humans table"
    (should= [{:table_name "users", :table_schem nil}] (db/find-dependent-tables {:table_name "humans"})))

  (it "should find dependent tables for cars table"
    (should= [{:table_name "humans", :table_schem nil}] (db/find-dependent-tables {:table_name "cars"})))

  (it "should find dependent tables for pets table"
    (should= [{:table_name "humans", :table_schem nil}] (db/find-dependent-tables {:table_name "pets"}))))

(describe "ordered-tables-list"
  (before cf/setup-test-db)

  (xit "should find the correct table order"
    (let [table-names (map :table_name (db/ordered-tables-list [] db/tables-list))]
      (should= ["users" "colors" "humans" "pets" "cars"] table-names))))

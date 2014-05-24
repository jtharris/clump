(ns clump.import-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clump.db :as db]
            [clump.import :as ci]))

(describe "table-name-from-file"
  (it "should strip .csv extension"
     (should= "dinosaurs" (ci/table-name-from-file (io/file "dinosaurs.csv"))))

  (it "should only strip .csv extension when dots in filename"
     (should= "schema.database" (ci/table-name-from-file (io/file "schema.database.csv")))))


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

; Functional Tests
(describe "Import CSVs"
  (before-all
    (setup-test-db)
    (ci/import-csvs
      (io/resource "../resources/input")))

  (it "should import the car data correctly"
    (let [car-data (j/query db/target-db "select * from cars where owner_id = 1 order by id")]
      (should= 2 (count car-data))
      (should= {:id 1 :year 1983 :make "Ford" :model "Thunderbird" :owner_id 1} (first car-data))
      (should= {:id 2 :year 2008 :make "Honda" :model "Civic" :owner_id 1} (last car-data)))

    (let [car-data (j/query db/target-db "select * from cars where owner_id = 2")]
      (should= 1 (count car-data))
      (should= {:id 3 :year 2002 :make "Hyundai" :model "Elantra" :owner_id 2} (first car-data))))

  (it "should import the user data correctly"
    (let [user-data (j/query db/target-db "select * from users")]
      (should= 4 (count user-data))
      (should= {:id 1 :name "Johnny Hotrod" :email "johnnyhotrod@hotmail.com"   } (nth user-data 0))
      (should= {:id 2 :name "Sally Twotone" :email "sallyride@gmail.com"        } (nth user-data 1))
      (should= {:id 3 :name "Billy Mayes"   :email "heybillymayeshere@yahoo.com"} (nth user-data 2))
      (should= {:id 4 :name "Someone Else"  :email "someotherguy@gmail.com"     } (nth user-data 3)))))

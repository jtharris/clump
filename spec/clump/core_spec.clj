(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clump.core :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.java.io :as io]))

(describe "table-name-from-file"
  (it "should strip .csv extension"
     (should= "dinosaurs" (table-name-from-file (clojure.java.io/file "dinosaurs.csv"))))

  (it "should only strip .csv extension when dots in filename"
     (should= "schema.database" (table-name-from-file (clojure.java.io/file "schema.database.csv")))))

(describe "table-name-from-map"
   (it "should allow no schema"
     (should= "[testtable]" (table-name-from-map {:table_name "testtable"}))
     (should= "[testtable]" (table-name-from-map {:table_name "testtable" :table_schem nil})))

   (it "should separate schema and table name with a ."
       (should= "[testingschema].[testingtable]"
                (table-name-from-map {:table_name "testingtable" :table_schem "testingschema"}))))

(describe "file-name"
   (it "should allow no schema"
     (should= "testtable.csv" (file-name {:table_name "testtable"}))
     (should= "testtable.csv" (file-name {:table_name "testtable" :table_schem nil})))

   (it "should separate schema and table name with a ."
       (should= "testingschema.testingtable.csv"
                (file-name {:table_name "testingtable" :table_schem "testingschema"}))))

(defn setup-test-db
  []
  (j/db-do-commands target-db "PRAGMA foreign_keys = OFF;")
  (doseq [table-name (map :table_name tables-list)]
    (j/db-do-commands target-db (j/drop-table-ddl table-name)))

  (j/db-do-commands target-db
    (j/create-table-ddl :users
                        [:id :integer "PRIMARY KEY"]
                        [:name :text]
                        [:email :text]))

  (j/db-do-commands target-db
    (j/create-table-ddl :humans
                        [:id :integer "PRIMARY KEY"]
                        [:user_id :integer "REFERENCES users"]
                        [:age :integer]))

  (j/db-do-commands target-db
    (j/create-table-ddl :pets
                        [:id :integer "PRIMARY KEY"]
                        [:species :text]
                        [:name :text]
                        [:owner_id :integer "REFERENCES humans"]))

  (j/db-do-commands target-db
    (j/create-table-ddl :cars
                        [:id :integer "PRIMARY KEY"]
                        [:year :integer]
                        [:make :text]
                        [:model :text]
                        [:owner_id :integer "REFERENCES humans"])))

  (j/db-do-commands target-db "PRAGMA foreign_keys = ON;")

; Functional Tests
(describe "Import CSVs"
  (before-all
    (setup-test-db)
    (import-csvs
      (io/resource "../resources/input")))

  (it "should import the car data correctly"
    (let [car-data (j/query target-db "select * from cars where owner_id = 1 order by id")]
      (should= 2 (count car-data))
      (should= {:id 1 :year 1983 :make "Ford" :model "Thunderbird" :owner_id 1} (first car-data))
      (should= {:id 2 :year 2008 :make "Honda" :model "Civic" :owner_id 1} (last car-data)))

    (let [car-data (j/query target-db "select * from cars where owner_id = 2")]
      (should= 1 (count car-data))
      (should= {:id 3 :year 2002 :make "Hyundai" :model "Elantra" :owner_id 2} (first car-data))))

  (it "should import the user data correctly"
    (let [user-data (j/query target-db "select * from users")]
      (should= 4 (count user-data))
      (should= {:id 1 :name "Johnny Hotrod" :email "johnnyhotrod@hotmail.com"   } (nth user-data 0))
      (should= {:id 2 :name "Sally Twotone" :email "sallyride@gmail.com"        } (nth user-data 1))
      (should= {:id 3 :name "Billy Mayes"   :email "heybillymayeshere@yahoo.com"} (nth user-data 2))
      (should= {:id 4 :name "Someone Else"  :email "someotherguy@gmail.com"     } (nth user-data 3)))))

(describe "Import/Export parity"
  (before-all
    (setup-test-db)
    (import-csvs
      (io/resource "../resources/input"))

    (export-csvs
      (io/resource "../resources/output")))

  (for [file ["cars.csv" "users.csv" "humans.csv"]]
    (it (str "should export " file " correctly")
      (should= (slurp (io/resource (str "resources/input/" file)))
               (slurp (io/resource (str "resources/output/" file)))))))

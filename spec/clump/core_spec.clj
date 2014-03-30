(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clump.core :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.java.io :as io]))

(describe "load-file"
 (with car-data (load-file "resources/input/cars.csv"))

 (it "can load the cars.csv from disc"
     (should= ["make"     "model"   "vin"          ] (nth @car-data 0))
     (should= ["Honda"    "Civic"   "3429834129"   ] (nth @car-data 1))
     (should= ["Honda"    "Accord"  "2393195"      ] (nth @car-data 2))
     (should= ["Ford"     "Mustang" "92384912cfds2"] (nth @car-data 3))
     (should= ["Audi"     "TT"      "98234dkfs"    ] (nth @car-data 4))
     (should= ["Toyota"   "MR2"     "9723jfa9"     ] (nth @car-data 5))
     (should= ["Mercades" "Benz"    "9839sdf"      ] (nth @car-data 6))))

(describe "table-name-from-file"
  (it "should strip .csv extension"
     (should= "dinosaurs" (table-name-from-file (clojure.java.io/file "dinosaurs.csv"))))

  (it "should only strip .csv extension when dots in filename"
     (should= "schema.database" (table-name-from-file (clojure.java.io/file "schema.database.csv")))))

; Functional Tests
(describe "Import CSVs"
  (before-all
    (j/delete! target-db :cars [])
    (j/delete! target-db :users [])

    (import-csvs
      (clojure.java.io/resource "../resources/input")))

  (it "should import the car data correctly"
    (let [car-data (j/query target-db "select * from cars")]
      (should= 6 (count car-data))
      (should= {:make "Honda"    :model "Civic"   :vin "3429834129"   } (nth car-data 0))
      (should= {:make "Honda"    :model "Accord"  :vin "2393195"      } (nth car-data 1))
      (should= {:make "Ford"     :model "Mustang" :vin "92384912cfds2"} (nth car-data 2))
      (should= {:make "Audi"     :model "TT"      :vin "98234dkfs"    } (nth car-data 3))
      (should= {:make "Toyota"   :model "MR2"     :vin "9723jfa9"     } (nth car-data 4))
      (should= {:make "Mercades" :model "Benz"    :vin "9839sdf"      } (nth car-data 5))))


  (it "should import the user data correctly"
    (let [user-data (j/query target-db "select * from users")]
      (should= 4 (count user-data))
      (should= {:first "Johnny"  :last "Hotrod"  :email "johnnyhotrod@hotmail.com"   } (nth user-data 0))
      (should= {:first "Sally"   :last "Twotone" :email "sallyride@gmail.com"        } (nth user-data 1))
      (should= {:first "Billy"   :last "Mayes"   :email "heybillymayeshere@yahoo.com"} (nth user-data 2))
      (should= {:first "Someone" :last "Else"    :email "someotherguy@gmail.com"     } (nth user-data 3)))))

(describe "Import/Export parity"
  (before-all
    (j/delete! target-db :cars [])
    (j/delete! target-db :users [])

    (import-csvs
      (clojure.java.io/resource "../resources/input"))

    (export-csvs
      (clojure.java.io/resource "../resources/output")))

  (it "should export cars.csv correctly"
    (should= (load-file "resources/input/cars.csv") (load-file "resources/output/cars.csv")))

  (it "should export users.csv correctly"
    (should= (load-file "resources/input/users.csv") (load-file "resources/output/users.csv"))))

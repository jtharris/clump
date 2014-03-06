(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clump.core :refer :all]))

(describe "load-file"
 (with car-data (load-file "resources/cars.csv"))

 (it "can load the cars.csv from disc"
     (should= ["make"     "model"   "vin"          ] (nth @car-data 0))
     (should= ["Honda"    "Civic"   "3429834129"   ] (nth @car-data 1))
     (should= ["Honda"    "Accord"  "2393195"      ] (nth @car-data 2))
     (should= ["Ford"     "Mustang" "92384912cfds2"] (nth @car-data 3))
     (should= ["Audi"     "TT"      "98234dkfs"    ] (nth @car-data 4))
     (should= ["Toyota"   "MR2"     "9723jfa9"     ] (nth @car-data 5))
     (should= ["Mercades" "Benz"    "9839sdf"      ] (nth @car-data 6))))

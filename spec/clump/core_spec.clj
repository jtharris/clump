(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clump.export :as ce]
            [clump.import :as ci]
            [clump.fixtures :as cf]))

(describe "Import/Export parity"
  (before-all
    (cf/setup-test-db)
    (ci/import-csvs
      (io/resource "../resources/input"))

    (ce/export-csvs
      (io/resource "../resources/output")))

  (for [file ["cars.csv" "users.csv" "humans.csv"]]
    (it (str "should export " file " correctly")
      (should= (slurp (io/resource (str "resources/input/" file)))
               (slurp (io/resource (str "resources/output/" file)))))))

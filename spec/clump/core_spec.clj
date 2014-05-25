(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clump.db :as db]
            [clump.export :as ce]
            [clump.import :as ci]
            [clump.db-spec :as db-spec]))

(describe "Import/Export parity"
  (before-all
    (db-spec/setup-test-db)
    (ci/import-csvs
      (io/resource "../resources/input"))

    (ce/export-csvs
      (io/resource "../resources/output")))

  (for [file ["cars.csv" "users.csv" "humans.csv"]]
    (it (str "should export " file " correctly")
      (should= (slurp (io/resource (str "resources/input/" file)))
               (slurp (io/resource (str "resources/output/" file)))))))

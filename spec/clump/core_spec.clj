(ns clump.core-spec
  (:require [speclj.core :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as s]
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
      (should= (s/split-lines (slurp (str "resources/input/" file)))
               (s/split-lines (slurp (str "resources/output/" file)))))))

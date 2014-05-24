(ns clump.export-spec
  (:require [speclj.core :refer :all]
            [clump.export :as ce]))

(describe "table-name-from-map"
   (it "should allow no schema"
     (should= "[testtable]" (ce/table-name-from-map {:table_name "testtable"}))
     (should= "[testtable]" (ce/table-name-from-map {:table_name "testtable" :table_schem nil})))

   (it "should separate schema and table name with a ."
       (should= "[testingschema].[testingtable]"
                (ce/table-name-from-map {:table_name "testingtable" :table_schem "testingschema"}))))

(describe "file-name"
   (it "should allow no schema"
     (should= "testtable.csv" (ce/file-name {:table_name "testtable"}))
     (should= "testtable.csv" (ce/file-name {:table_name "testtable" :table_schem nil})))

   (it "should separate schema and table name with a ."
       (should= "testingschema.testingtable.csv"
                (ce/file-name {:table_name "testingtable" :table_schem "testingschema"}))))

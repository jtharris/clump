(ns clump.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clump.export]
            [clump.import]))

(def config (edn/read-string (slurp "resources/config.edn")))

(defn -main
  [& args]

  (let [action (first args)]
    (cond
     (= action "import") (clump.import/import-csvs (io/resource (str "../" (:input-dir config))))
     (= action "export") (clump.export/export-csvs (io/resource (str "../" (:output-dir config))))
     :else (println "Please specify 'import' or 'export'"))))




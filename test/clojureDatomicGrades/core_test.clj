(ns clojureDatomicGrades.core-test
  (:require [expectations :refer :all]
            [clojureDatomicGrades.core :refer :all]
            [datomic.api :as d]
            ))

(defn create-empty-in-memory-db []
  (let [uri "datomic:mem://grades-db"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/datomic/schema.edn")]
      (d/transact conn schema)
      conn
      )
    ))



(expect [["Calculus" 0.4] ]
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission3" "Jack" "Assignment1")
          (add-submission "Submission5" "Suze" "Assignment2")
          (add-submission "Submission7" "Jack" "Assignment2")
          (unit-mean-grades ["Calculus"]))))





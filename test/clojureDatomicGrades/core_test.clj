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


;; Students
(expect #{["John"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "John")
          (find-all-students))))

(expect #{["John"] ["Suze"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "John")
          (add-student "Suze")
          (find-all-students))))

;; Units
(expect #{["Calculus"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (find-all-units))))

(expect #{["Calculus"] ["Exam"] ["Practical"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit2 "Exam" "Calculus")
          (add-unit2 "Practical" "Calculus")
          (find-all-units))))

(expect #{["Exam"] ["Practical"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit2 "Exam" "Calculus")
          (add-unit2 "Practical" "Calculus")
          (find-all-units-in "Calculus"))))

(expect #{["Exam"] ["Practical"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit2 "Exam" "Calculus")
          (add-unit2 "Practical" "Calculus")
          (add-unit2 "Part1" "Practical")
          (add-unit2 "Part2" "Practical")
          (find-all-units-in "Calculus"))))

(expect #{["Exam"] ["Practical"] ["Part1"] ["Part2"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit2 "Exam" "Calculus")
          (add-unit2 "Practical" "Calculus")
          (add-unit2 "Part1" "Practical")
          (add-unit2 "Part2" "Practical")
          (find-all-units-in-recursive "Calculus"))))

;; Assignments
(expect #{["Assignment1"] ["Assignment2"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (find-all-assignments))))

(expect #{["Assignment1"] ["Assignment2"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (find-all-assignments-in "Calculus"))))

(expect #{}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit "Database Theory")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (find-all-assignments-in "Database Theory"))))

(expect #{["Assignment1"] ["Assignment2"] ["Exam"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit2 "Practical" "Calculus")
          (add-assignment "Assignment1" "Practical")
          (add-assignment "Assignment2" "Practical")
          (add-assignment "Exam" "Calculus")
          (find-all-assignments-in-recursive "Calculus"))))

;; Submissions
(expect #{["Submission1"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (find-all-submissions))))

(expect #{["Submission1"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (find-all-submissions-assignment "Assignment1"))))

(expect #{["Submission1"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission2" "Jack" "Assignment1")
          (find-all-submissions-student "Suze"))))

(expect #{["Submission1"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission2" "Jack" "Assignment1")
          (add-submission "Submission3" "Suze" "Assignment2")
          (find-submission-student-assignment "Suze" "Assignment1"))))


;; Grades
(expect 0.1
        (calculate-grade "Submission1"))
(expect 9.9
        (calculate-grade "Submission99"))
(expect 3.7
        (calculate-grade "Submission1337"))

(expect #{[0.1]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (submission-grade "Suze" "Assignment1"))))

(expect #{["Suze" 0.1] ["Jack" 0.2]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission2" "Jack" "Assignment1")
          (assignment-grades "Assignment1"))))

(expect [[0.2]]
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission3" "Jack" "Assignment1")
          (assignment-mean-grade "Assignment1"))))

(expect [["Assignment1" 0.2] ["Assignment2" 0.6]]
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
          (assignment-mean-grades ["Assignment1" "Assignment2"]))))

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





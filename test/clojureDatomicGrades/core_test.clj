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


(expect #{["John"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "John" conn)
          (find-all-students conn)))

(expect #{["John"] ["Suze"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "John" conn)
          (add-student "Suze" conn)
          (find-all-students conn)))

;; Units
(expect #{["Calculus"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (find-all-units conn)))

(expect #{["Calculus"] ["Exam"] ["Practical"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit2 "Exam" "Calculus" conn)
          (add-unit2 "Practical" "Calculus" conn)
          (find-all-units conn)))

(expect #{["Exam"] ["Practical"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit2 "Exam" "Calculus" conn)
          (add-unit2 "Practical" "Calculus" conn)
          (find-all-units-in "Calculus" conn)))

(expect #{["Exam"] ["Practical"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit2 "Exam" "Calculus" conn)
          (add-unit2 "Practical" "Calculus" conn)
          (add-unit2 "Part1" "Practical" conn)
          (add-unit2 "Part2" "Practical" conn)
          (find-all-units-in "Calculus" conn)))

(expect #{["Exam"] ["Practical"] ["Part1"] ["Part2"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit2 "Exam" "Calculus" conn)
          (add-unit2 "Practical" "Calculus" conn)
          (add-unit2 "Part1" "Practical" conn)
          (add-unit2 "Part2" "Practical" conn)
          (find-all-units-in-recursive "Calculus" conn)))

;; Assignments
(expect #{["Assignment1"] ["Assignment2"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (find-all-assignments conn)))

(expect #{["Assignment1"] ["Assignment2"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (find-all-assignments-in "Calculus" conn)))

(expect #{}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit "Database Theory" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (find-all-assignments-in "Database Theory" conn)))

(expect #{["Assignment1"] ["Assignment2"] ["Exam"]}
        (let [conn (create-empty-in-memory-db)]
          (add-unit "Calculus" conn)
          (add-unit2 "Practical" "Calculus" conn)
          (add-assignment "Assignment1" "Practical" conn)
          (add-assignment "Assignment2" "Practical" conn)
          (add-assignment "Exam" "Calculus" conn)
          (find-all-assignments-in-recursive "Calculus" conn)))

;; Submissions
(expect #{["Submission1"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (find-all-submissions conn)))

(expect #{["Submission1"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (find-all-submissions-assignment "Assignment1" conn)))

(expect #{["Submission1"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission2" "Jack" "Assignment1" conn)
          (find-all-submissions-student "Suze" conn)))

(expect #{["Submission1"]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission2" "Jack" "Assignment1" conn)
          (add-submission "Submission3" "Suze" "Assignment2" conn)
          (find-submission-student-assignment "Suze" "Assignment1" conn)))


;; Grades
(expect 0.1
        (calculate-grade "Submission1"))
(expect 9.9
        (calculate-grade "Submission99"))
(expect 3.7
        (calculate-grade "Submission1337"))

(expect #{[0.1]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (submission-grade "Suze" "Assignment1" conn)))

(expect #{["Suze" 0.1] ["Jack" 0.2]}
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission2" "Jack" "Assignment1" conn)
          (assignment-grades "Assignment1" conn)))

(expect [[0.2]]
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission3" "Jack" "Assignment1" conn)
          (assignment-mean-grade "Assignment1" conn)))

(expect [["Assignment1" 0.2] ["Assignment2" 0.6]]
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission3" "Jack" "Assignment1" conn)
          (add-submission "Submission5" "Suze" "Assignment2" conn)
          (add-submission "Submission7" "Jack" "Assignment2" conn)
          (assignment-mean-grades ["Assignment1" "Assignment2"] conn)))

(expect [["Calculus" 0.4] ]
        (let [conn (create-empty-in-memory-db)]
          (add-student "Suze" conn)
          (add-student "Jack" conn)
          (add-unit "Calculus" conn)
          (add-assignment "Assignment1" "Calculus" conn)
          (add-assignment "Assignment2" "Calculus" conn)
          (add-submission "Submission1" "Suze" "Assignment1" conn)
          (add-submission "Submission3" "Jack" "Assignment1" conn)
          (add-submission "Submission5" "Suze" "Assignment2" conn)
          (add-submission "Submission7" "Jack" "Assignment2" conn)
          (unit-mean-grades ["Calculus"] conn)))






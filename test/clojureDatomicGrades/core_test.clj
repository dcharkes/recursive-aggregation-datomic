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

(expect #{["John"] ["Suze"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-students ["John" "Suze"])
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
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (find-all-units))))

(expect #{["Exam"] ["Practical"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (find-all-units-in "Calculus"))))

(expect #{["Exam"] ["Practical"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (add-unit "Part1" "Practical")
          (add-unit "Part2" "Practical")
          (find-all-units-in "Calculus"))))

(expect #{["Exam"] ["Practical"] ["Part1"] ["Part2"]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (add-unit "Part1" "Practical")
          (add-unit "Part2" "Practical")
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
          (add-unit "Practical" "Calculus")
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

;; Grades: Assignment means
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

;; Grades: Unit means
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
          (let [db (d/db conn)]
          (unit-mean-grades db ["Calculus"])))))

(expect [["Calculus" 0.5] ]
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (add-assignment "Assignment1" "Practical")
          (add-assignment "Exam" "Exam")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission9" "Suze" "Exam")
          (let [db (d/db conn)]
          (unit-mean-grades db ["Calculus"])))))

(expect [["Calculus" 0.4] ["Exam" 0.6] ["Practical" 0.2] ]
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (add-assignment "Assignment1" "Practical")
          (add-assignment "Assignment2" "Practical")
          (add-assignment "Exam" "Exam")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission1" "Jack" "Assignment1")
          (add-submission "Submission2" "Suze" "Assignment2")
          (add-submission "Submission4" "Jack" "Assignment2")
          (add-submission "Submission5" "Suze" "Exam")
          (add-submission "Submission7" "Jack" "Exam")
          (let [db (d/db conn)]
          (unit-mean-grades db ["Calculus" "Exam" "Practical"])))))

(expect [["Calculus" 0.4] ["Exam" 0.6] ["Practical" 0.2] ]
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Joey")
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-assignment "Exam" "Exam")
          (add-unit "Practical" "Calculus")
          (add-unit "Assignment1" "Practical")
          (add-unit "Assignment1.1" "Assignment1")
          (add-assignment "Assignment1.1.1" "Assignment1.1")
          (add-assignment "Assignment1.1.2" "Assignment1.1")
          (add-assignment "Assignment1.2" "Assignment1")
          (add-assignment "Assignment2" "Practical")
          (add-submission "Submission1" "Suze" "Assignment1.1.1")
          (add-submission "Submission1" "Joey" "Assignment1.1.1")
          (add-submission "Submission1" "Suze" "Assignment1.1.2")
          (add-submission "Submission1" "Joey" "Assignment1.1.2")
          (add-submission "Submission1" "Suze" "Assignment1.2")
          (add-submission "Submission1" "Joey" "Assignment1.2")
          (add-submission "Submission1" "Suze" "Assignment2")
          (add-submission "Submission5" "Joey" "Assignment2")
          (add-submission "Submission5" "Suze" "Exam")
          (add-submission "Submission7" "Joey" "Exam")
          (let [db (d/db conn)]
          (unit-mean-grades db ["Calculus" "Exam" "Practical"])))))

;; Grades: Student Assignments
(expect #{[0.1]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (let [db (d/db conn)]
          (student-assignment-grade db "Suze" "Assignment1")))))

(expect #{[9.9]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Jack")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission99" "Jack" "Assignment1")
          (let [db (d/db conn)]
          (student-assignment-grade db "Jack" "Assignment1")))))

(expect #{[5.5]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission55" "Suze" "Assignment1")
          (let [db (d/db conn)]
          (student-assignment-grade db "Suze" "Assignment1")))))

;; Grades: Student Unit means
(expect #{[0.3]}
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
          (let [db (d/db conn)]
          (student-unit-mean-grade db "Suze" "Calculus")))))

(expect #{[5.5]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-assignment "Assignment1" "Calculus")
          (add-assignment "Assignment2" "Calculus")
          (add-submission "Submission33" "Suze" "Assignment1")
          (add-submission "Submission77" "Suze" "Assignment2")
          (let [db (d/db conn)]
          (student-unit-mean-grade db "Suze" "Calculus")))))

(expect #{[0.5]}
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-unit "Practical" "Calculus")
          (add-assignment "Assignment1" "Practical")
          (add-assignment "Exam" "Exam")
          (add-submission "Submission1" "Suze" "Assignment1")
          (add-submission "Submission9" "Suze" "Exam")
          (let [db (d/db conn)]
          (student-unit-mean-grade db "Suze" "Calculus")))))

(expect (list #{[0.3]} #{[0.5]})
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-student "Suze")
          (add-student "Joey")
          (add-unit "Calculus")
          (add-unit "Exam" "Calculus")
          (add-assignment "Exam" "Exam")
          (add-unit "Practical" "Calculus")
          (add-unit "Assignment1" "Practical")
          (add-unit "Assignment1.1" "Assignment1")
          (add-assignment "Assignment1.1.1" "Assignment1.1")
          (add-assignment "Assignment1.1.2" "Assignment1.1")
          (add-assignment "Assignment1.2" "Assignment1")
          (add-assignment "Assignment2" "Practical")
          (add-submission "Submission1" "Suze" "Assignment1.1.1")
          (add-submission "Submission1" "Joey" "Assignment1.1.1")
          (add-submission "Submission1" "Suze" "Assignment1.1.2")
          (add-submission "Submission1" "Joey" "Assignment1.1.2")
          (add-submission "Submission1" "Suze" "Assignment1.2")
          (add-submission "Submission1" "Joey" "Assignment1.2")
          (add-submission "Submission1" "Suze" "Assignment2")
          (add-submission "Submission5" "Joey" "Assignment2")
          (add-submission "Submission5" "Suze" "Exam")
          (add-submission "Submission7" "Joey" "Exam")
          (let [db (d/db conn)]
          (list
           (student-unit-mean-grade db "Suze" "Calculus")
           (student-unit-mean-grade db "Joey" "Calculus"))))))



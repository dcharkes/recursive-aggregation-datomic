(ns clojureDatomicGrades.core
  (:require [datomic.api :as d])
  )

;; Database connection

;(def uri "datomic:free://localhost:4334/grades-db")
;(def conn (d/connect uri))
(def conn nil)


;; Helper functions : Database
(defn find-student-id [name]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :student/name ?name]]
              (d/db conn)
              name)))

(defn find-unit-id [name]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :unit/name ?name]]
              (d/db conn)
              name)))

(defn find-assignment-id [name]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :assignment/name ?name]]
              (d/db conn)
              name)))


;; Helper functions : other
(defn round [s n]
  (.setScale (bigdec n) s java.math.RoundingMode/HALF_EVEN))


;; Students
(defn add-student [name]
  @(d/transact conn [{:db/id (d/tempid :db.part/user)
                      :student/name name}]))




;; Units
(defn add-unit [name]
  @(d/transact conn [{:db/id (d/tempid :db.part/user)
                      :unit/name name}]))



;; Assignments
(defn add-assignment [name unit-name]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :assignment/name name}
                      {:db/id (find-unit-id unit-name)
                       :unit/children id}])))


;; Submissions
(defn add-submission [data student-name assignment-name]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :submission/data data}
                      {:db/id id
                       :submission/student (find-student-id student-name)}
                      {:db/id id
                       :submission/assignment (find-assignment-id assignment-name)}])))




;; Grades
(defn calculate-grade [data] ;data is formatted like : Submission1
  (* 1.0 (mod (/ (read-string (subs data 10)) 10) 10)))

(defn submission-grade [student-name assignment-name]
  (d/q '[:find ?grade
         :in $ % ?student-name ?assignment-name
         :where
         [?a :assignment/name ?assignment-name]
         [?st :student/name ?student-name]
         [?s :submission/student ?st]
         [?s :submission/assignment ?a]
         (submission/grade ?s ?grade)
         ]
       (d/db conn)
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       student-name
       assignment-name))


(defn assignment-mean-grade2 [assignment-id]
  (ffirst
  (d/q '[:find (avg ?grade)
         :in $ % ?a
         :where
         [?s :submission/assignment ?a]
         (submission/grade ?s ?grade)
         ]
       (d/db conn)
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       assignment-id)))

(defn unit-mean-grades [unit-names]
  (d/q '[:find ?unit-name (avg ?mean-grade)
         :in $ % [?unit-name ...]
         :where
         [?u :unit/name ?unit-name]
         [?u :unit/children ?a]
         (assignment/meangrade12 ?a ?mean-grade) ;the number has to be updated each time to circument an error
         ]
       (d/db conn)
       '[[(assignment/meangrade12 ?a ?mean-grade) ;idem
           [(clojureDatomicGrades.core/assignment-mean-grade2 ?a) ?mean-grade]
          ]
         ]
       unit-names))





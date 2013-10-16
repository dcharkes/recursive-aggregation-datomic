(ns clojureDatomicGrades.core
  (:require [datomic.api :as d])
  )

;; Database connection

;(def uri "datomic:free://localhost:4334/grades-db")
;(def conn (d/connect uri))
;(def conn nil)


;; Helper functions : Database
(defn find-student-id [name conn]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :student/name ?name]]
              (d/db conn)
              name)))

(defn find-unit-id [name conn]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :unit/name ?name]]
              (d/db conn)
              name)))

(defn find-assignment-id [name conn]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where [?id :assignment/name ?name]]
              (d/db conn)
              name)))


;; Helper functions : other
(defn round [s n]
  (.setScale (bigdec n) s java.math.RoundingMode/HALF_EVEN))


;; Students
(defn add-student [name conn]
  @(d/transact conn [{:db/id (d/tempid :db.part/user)
                      :student/name name}]))

(defn find-all-students [conn]
  (d/q '[:find ?name
         :where [_ :student/name ?name]]
       (d/db conn)))


;; Units
(defn add-unit [name conn]
  @(d/transact conn [{:db/id (d/tempid :db.part/user)
                      :unit/name name}]))

(defn add-unit2 [name parent-name conn]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :unit/name name}
                      {:db/id (find-unit-id parent-name conn)
                       :unit/children id}])))

(defn find-all-units [conn]
  (d/q '[:find ?name
         :where [_ :unit/name ?name]]
       (d/db conn)))

(defn find-all-units-in [parent-name conn]
  (d/q '[:find ?name
         :in $ ?parent-name
         :where
         [?u :unit/name ?name]
         [?p :unit/children ?u]
         [?p :unit/name ?parent-name]
         ]
       (d/db conn)
       parent-name))

(defn find-all-units-in-recursive [parent-name conn]
  (d/q '[:find ?name
         :in $ % ?parent-name
         :where
         [?u :unit/name ?name]
         (unit/in ?p ?u)
         [?p :unit/name ?parent-name]
         ]
       (d/db conn)
       '[[(unit/in ?p ?c)
             [?p :unit/children ?c]]
         [(unit/in ?a ?c)
             (unit/in ?a ?p)
             [?p :unit/children ?c]]
         ]
       parent-name))


;; Assignments
(defn add-assignment [name unit-name conn]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :assignment/name name}
                      {:db/id (find-unit-id unit-name conn)
                       :unit/children id}])))

(defn find-all-assignments [conn]
  (d/q '[:find ?name
         :where [_ :assignment/name ?name]]
       (d/db conn)))

(defn find-all-assignments-in [unit-name conn]
  (d/q '[:find ?name
         :in $ ?unit-name
         :where
         [?a :assignment/name ?name]
         [?p :unit/children ?a]
         [?p :unit/name ?unit-name]
         ]
       (d/db conn)
       unit-name))

(defn find-all-assignments-in-recursive [unit-name conn]
  (d/q '[:find ?name
         :in $ % ?unit-name
         :where
         [?a :assignment/name ?name]
         (unit/in ?p ?a)
         [?p :unit/name ?unit-name]
         ]
       (d/db conn)
       '[[(unit/in ?p ?c)
             [?p :unit/children ?c]]
         [(unit/in ?a ?c)
             (unit/in ?a ?p)
             [?p :unit/children ?c]]
         ]
       unit-name))


;; Submissions
(defn add-submission [data student-name assignment-name conn]
  (let [id (d/tempid :db.part/user)
        student-id (find-student-id student-name conn)
        assignment-id (find-assignment-id assignment-name conn)]
    @(d/transact conn [{:db/id id
                       :submission/data data}
                      {:db/id id
                       :submission/student student-id}
                      {:db/id id
                       :submission/assignment assignment-id}])))

(defn find-all-submissions [conn]
  (d/q '[:find ?data
         :where [_ :submission/data ?data]]
       (d/db conn)))

(defn find-all-submissions-assignment [name conn]
  (d/q '[:find ?data
         :in $ ?name
         :where
         [?s :submission/data ?data]
         [?s :submission/assignment ?a]
         [?a :assignment/name ?name]]
       (d/db conn)
       name))

(defn find-all-submissions-student [name conn]
  (d/q '[:find ?data
         :in $ ?name
         :where
         [?s :submission/data ?data]
         [?s :submission/student ?st]
         [?st :student/name ?name]]
       (d/db conn)
       name))

(defn find-submission-student-assignment [student-name assignment-name conn]
  (d/q '[:find ?data
         :in $ ?student-name ?assignment-name
         :where
         [?a :assignment/name ?assignment-name]
         [?st :student/name ?student-name]
         [?s :submission/student ?st]
         [?s :submission/assignment ?a]
         [?s :submission/data ?data]
         ]
       (d/db conn)
       student-name
       assignment-name))


;; Grades
(defn calculate-grade [data] ;data is formatted like : Submission1
  (* 1.0 (mod (/ (read-string (subs data 10)) 10) 10)))

(defn submission-grade [student-name assignment-name conn]
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

(defn assignment-grades [assignment-name conn]
  (d/q '[:find ?student-name ?grade
         :in $ % ?assignment-name
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
       assignment-name))

(defn assignment-mean-grade [assignment-name conn]
  (d/q '[:find (avg ?grade)
         :in $ % ?assignment-name
         :where
         [?a :assignment/name ?assignment-name]
         [?s :submission/assignment ?a]
         (submission/grade ?s ?grade)
         ]
       (d/db conn)
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       assignment-name))

(defn assignment-mean-grades [assignment-names conn]
  (d/q '[:find ?assignment-name (avg ?grade)
         :in $ % [?assignment-name ...]
         :where
         [?a :assignment/name ?assignment-name]
         [?s :submission/assignment ?a]
         (submission/grade ?s ?grade)
         ]
       (d/db conn)
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       assignment-names))


(defn assignment-mean-grade2 [assignment-id conn]
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

(defn unit-mean-grades [unit-names conn]
  (d/q '[:find ?unit-name (avg ?mean-grade)
         :in $ % [?unit-name ...]
         :where
         [?u :unit/name ?unit-name]
         [?u :unit/children ?a]
         (assignment/meangrade13 ?a ?mean-grade)
         ]
       (d/db conn)
       '[[(assignment/meangrade13 ?a ?mean-grade)
           [(clojureDatomicGrades.core/assignment-mean-grade2 ?a conn) ?mean-grade]; <-- this conn is undefined
          ]
         ]
       unit-names))





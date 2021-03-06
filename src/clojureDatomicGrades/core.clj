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

(defn add-students [names]
  @(d/transact
    conn
    (map #(identity {:db/id (d/tempid :db.part/user)
                     :student/name %}) names)))

(defn find-all-students []
  (d/q '[:find ?name
         :where [_ :student/name ?name]]
       (d/db conn)))


;; Units
(defn add-unit
  ([name]
   @(d/transact conn [{:db/id (d/tempid :db.part/user)
                       :unit/name name}])
   )
  ([name parent-name]
   (let [id (d/tempid :db.part/user)]
     @(d/transact conn [{:db/id id
                         :unit/name name}
                        {:db/id (find-unit-id parent-name)
                         :unit/children id}]))
   ))

(defn add-units
  [names-and-parent-names]

   ; grabbing the first works
;;    (let [id (d/tempid :db.part/user)]
;;      @(d/transact conn [{:db/id id
;;                          :unit/name (ffirst names-and-parent-names)}
;;                         {:db/id (find-unit-id (second (first names-and-parent-names)))
;;                          :unit/children id}]))

   ; grabbing the first works
;;    (let [id (d/tempid :db.part/user)]
;;                        [{:db/id id
;;                          :unit/name (ffirst names-and-parent-names)}
;;                         {:db/id (find-unit-id (second (first names-and-parent-names)))
;;                          :unit/children id}])

   ; wrapping in a map thows a null pointer
;;    (map
;;     #(let [id (d/tempid :db.part/user)]
;;                       [{:db/id id
;;                         :unit/name (first %)}
;;                        {:db/id (find-unit-id (second %))
;;                         :unit/children id}])
;;     names-and-parent-names)

  )

(defn find-all-units []
  (d/q '[:find ?name
         :where [_ :unit/name ?name]]
       (d/db conn)))

(defn find-all-units-in [parent-name]
  (d/q '[:find ?name
         :in $ ?parent-name
         :where
         [?u :unit/name ?name]
         [?p :unit/children ?u]
         [?p :unit/name ?parent-name]
         ]
       (d/db conn)
       parent-name))

(defn find-all-units-in-recursive [parent-name]
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
(defn add-assignment [name unit-name]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :assignment/name name}
                      {:db/id (find-unit-id unit-name)
                       :unit/children id}])))

(defn find-all-assignments []
  (d/q '[:find ?name
         :where [_ :assignment/name ?name]]
       (d/db conn)))

(defn find-all-assignments-in [unit-name]
  (d/q '[:find ?name
         :in $ ?unit-name
         :where
         [?a :assignment/name ?name]
         [?p :unit/children ?a]
         [?p :unit/name ?unit-name]
         ]
       (d/db conn)
       unit-name))

(defn find-all-assignments-in-recursive [unit-name]
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
(defn add-submission [data student-name assignment-name]
  (let [id (d/tempid :db.part/user)]
    @(d/transact conn [{:db/id id
                       :submission/data data}
                      {:db/id id
                       :submission/student (find-student-id student-name)}
                      {:db/id id
                       :submission/assignment (find-assignment-id assignment-name)}])))

(defn find-all-submissions []
  (d/q '[:find ?data
         :where [_ :submission/data ?data]]
       (d/db conn)))

(defn find-all-submissions-assignment [name]
  (d/q '[:find ?data
         :in $ ?name
         :where
         [?s :submission/data ?data]
         [?s :submission/assignment ?a]
         [?a :assignment/name ?name]]
       (d/db conn)
       name))

(defn find-all-submissions-student [name]
  (d/q '[:find ?data
         :in $ ?name
         :where
         [?s :submission/data ?data]
         [?s :submission/student ?st]
         [?st :student/name ?name]]
       (d/db conn)
       name))

(defn find-submission-student-assignment [student-name assignment-name]
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


;; Grades: Submissions
(defn calculate-grade [data] ;data is formatted like : Submission1
  (-> data (subs 10) (read-string) (/ 10) (mod 10) (* 1.0)))

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

;; Grades: Assignment means
(defn assignment-grades [assignment-name]
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

(defn assignment-mean-grade [assignment-name]
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

(defn assignment-mean-grades [assignment-names]
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


(defn assignment-mean-grade-on-id [db assignment-id]
  (ffirst
  (d/q '[:find (avg ?grade)
         :in $ % ?a
         :where
         [?s :submission/assignment ?a]
         (submission/grade ?s ?grade)
         ]
       db
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       assignment-id)))

;; Grades: Unit means
(defn unit-mean-grade-on-id [db unit-id]
  (ffirst
  (d/q '[:find (avg ?mean-grade)
         :in $ % ?u
         :where
         (unit/meangrade ?u ?mean-grade)
         ]
       db
       '[[(unit/meangrade ?u ?mean-grade)
           [?u :unit/children ?a]
           [?a :assignment/name _]
           [(clojureDatomicGrades.core/assignment-mean-grade-on-id $ ?a) ?mean-grade]
          ]
         [(unit/meangrade ?u ?mean-grade)
           [?u :unit/children ?c]
           [?c :unit/name _]
           [(clojureDatomicGrades.core/unit-mean-grade-on-id $ ?c) ?mean-grade]
          ]
         ]
       unit-id)))

(defn unit-mean-grades [db unit-names]
  (d/q '[:find ?unit-name (avg ?mean-grade)
         :in $ % [?unit-name ...]
         :where
         [?u :unit/name ?unit-name]
         (unit/meangrade-on-id ?u ?mean-grade)
         ]
       db
       '[[(unit/meangrade-on-id ?a ?mean-grade)
           [(clojureDatomicGrades.core/unit-mean-grade-on-id $ ?a) ?mean-grade]
          ]
         ]
       unit-names))

;; Grades: Student Assignments
(defn student-assignment-grade-on-id [db assignment-id student-id]
  (ffirst
  (d/q '[:find ?grade
         :in $ % ?a ?st
         :where
         [?s :submission/assignment ?a]
         [?s :submission/student ?st]
         (submission/grade ?s ?grade)
         ]
       db
       '[[(submission/grade ?s ?grade)
           [?s :submission/data ?data]
           [(clojureDatomicGrades.core/calculate-grade ?data) ?grade]]
         ]
       assignment-id
       student-id)))

(defn student-assignment-grade [db student-name assignment-name]
  (d/q '[:find ?grade
         :in $ % ?student-name ?assignment-name
         :where
         [?a :assignment/name ?assignment-name]
         [?st :student/name ?student-name]
         (student-assignment/grade ?a ?st ?grade)
         ]
       db
       '[[(student-assignment/grade ?a ?st ?grade)
           [(clojureDatomicGrades.core/student-assignment-grade-on-id $ ?a ?st) ?grade]]
         ]
       student-name
       assignment-name))

;; Grades: Student Unit means
(defn student-unit-mean-grade-on-id [db unit-id student-id]
  (ffirst
  (d/q '[:find (avg ?mean-grade)
         :in $ % ?u ?st
         :where
         (student-unit/meangrade ?u ?st ?mean-grade)
         ]
       db
       '[[(student-unit/meangrade ?u ?st ?mean-grade)
           [?u :unit/children ?a]
           [?a :assignment/name _]
           [(clojureDatomicGrades.core/student-assignment-grade-on-id $ ?a ?st) ?mean-grade]
          ]
         [(student-unit/meangrade ?u ?st ?mean-grade)
           [?u :unit/children ?c]
           [?c :unit/name _]
           [(clojureDatomicGrades.core/student-unit-mean-grade-on-id $ ?c ?st) ?mean-grade]
          ]
         ]
       unit-id
       student-id)))

(defn student-unit-mean-grade [db student-name unit-name]
  (d/q '[:find ?mean-grade
         :in $ % ?unit-name ?student-name
         :where
         [?u :unit/name ?unit-name]
         [?st :student/name ?student-name]
         (student-unit/meangrade-on-id ?u ?st ?mean-grade)
         ]
       db
       '[[(student-unit/meangrade-on-id ?a ?st ?mean-grade)
           [(clojureDatomicGrades.core/student-unit-mean-grade-on-id $ ?a ?st) ?mean-grade]
          ]
         ]
       unit-name
       student-name))






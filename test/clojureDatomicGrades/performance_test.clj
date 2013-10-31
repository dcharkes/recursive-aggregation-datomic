(ns clojureDatomicGrades.performance-test
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

(defmacro profile [what expression]
  `(let [start# (System/nanoTime)
        return# ~expression
        end# (System/nanoTime)]
    (printf "%-20s %5.1f ms\n" ~what (/ (- end# start#) 1000000.0))
    return#))

(expect #{["John"]}
        (with-redefs [conn (profile "create in-memory-db" (create-empty-in-memory-db))]
        (do
          (add-student "John")
          (find-all-students))))

(expect (into #{} (map #(vector (str "Student" %)) (range 1 1001)))
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (profile
           "create 1000 students"
           (add-students (map #(str "Student" %) (range 1 1001))))
          (find-all-students)
          )))

(expect (into #{} (map #(vector (str "Student" %)) (range 1 1001)))
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (add-students (map #(str "Student" %) (range 1 1001)))
          (profile
           "read 1000 students"
           (find-all-students))
          )))

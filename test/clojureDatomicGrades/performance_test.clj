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

;; (expect #{["John"]}
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (profile "create 1 student" (add-student "John"))
;;           (find-all-students))))

;; (expect #{["Suze"] ["Jack"] ["Job"] ["Tanja"]}
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (profile
;;            "create 4 students"
;;            (do
;;              (add-student "Jack")
;;              (add-student "Suze")
;;              (add-student "Job")
;;              (add-student "Tanja")
;;              ))
;;           (find-all-students))))

;; (expect (into #{} (map #(vector (str "Student" %)) (range 1 11)))
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (profile
;;            "create 10 students"
;;            (doall (map #(add-student %) (map #(str "Student" %) (range 1 11)))))
;;           (find-all-students)
;;           )))

;; (expect (into #{} (map #(vector (str "Student" %)) (range 1 101)))
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (profile
;;            "create 100 students"
;;            (doall (map #(add-student %) (map #(str "Student" %) (range 1 101)))))
;;           (find-all-students)
;;           )))

(expect (into #{} (map #(vector (str "Student" %)) (range 1 1001)))
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (profile
           "create 1000 students"
           (doall (map #(add-student %) (map #(str "Student" %) (range 1 1001)))))
          (find-all-students)
          )))


;; (expect #{["Jack"]}
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (add-student "Jack")
;;           (profile "read 1 student" (find-all-students)))))

;; (expect #{["Suze"] ["Jack"] ["Job"] ["Tanja"]}
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (add-student "Jack")
;;           (add-student "Suze")
;;           (add-student "Job")
;;           (add-student "Tanja")
;;           (profile "read 4 students" (find-all-students)))))

;; (expect (into #{} (map #(vector (str "Student" %)) (range 1 11)))
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (doall (map #(add-student %) (map #(str "Student" %) (range 1 11))))
;;           (profile
;;            "read 10 students"
;;            (find-all-students))
;;           )))

;; (expect (into #{} (map #(vector (str "Student" %)) (range 1 101)))
;;         (with-redefs [conn (create-empty-in-memory-db)]
;;         (do
;;           (doall (map #(add-student %) (map #(str "Student" %) (range 1 101))))
;;           (profile
;;            "read 100 students"
;;            (find-all-students))
;;           )))

(expect (into #{} (map #(vector (str "Student" %)) (range 1 1001)))
        (with-redefs [conn (create-empty-in-memory-db)]
        (do
          (doall (map #(add-student %) (map #(str "Student" %) (range 1 1001))))
          (profile
           "read 1000 students"
           (find-all-students))
          )))

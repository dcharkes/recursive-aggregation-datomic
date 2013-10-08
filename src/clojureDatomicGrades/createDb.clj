(ns clojureDatomicGrades.createDb
  (:require [datomic.api :as d])
  )

;; helper file for manually importing the schema

; first run Datomic transactor: bin/transactor config/samples/free-transactor-template.properties

;(def uri "datomic:free://localhost:4334/pet-owners-db")
;(d/delete-database uri)
;(d/create-database uri)
;(let [conn (d/connect uri)
;      schema (load-file "resources/datomic/schema.edn")]
;  (d/transact conn schema)
;  conn
;)

(defproject clojureDatomicGrades "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.8.4215"]
                 [expectations "1.4.56"]
                 ]
  :plugins [[lein-autoexpect "1.0"]
            [lein-datomic "0.2.0"]]

  :datomic {:schemas ["resources/datomic" ["schema.edn"]]
            :install-location "C:/Program Files custom/datomic-free-0.8.4215"}
  :profiles {:dev
             {:datomic {:config "resources/datamic/free-transactor-template.properties"
                        :db-uri "datomic:free://localhost:4334/grades-db"}}}
)

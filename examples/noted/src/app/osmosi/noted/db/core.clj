(ns app.osmosi.noted.db.core
  (:require [next.jdbc :as db]))

(def mysql {:dbtype "mysql"
            :dbname "Planner"
            :host "192.168.1.112"
            :user "root"
            :useSSL "false"
            :password "changeme"})

(def ds (db/get-datasource mysql))

(defn get-tasks []
  (db/execute! ds ["select * from tasks"]))

(defn add-note [text]
  (db/execute-one! ds ["insert into tasks (text) values (?)" text] {:return-keys true}))

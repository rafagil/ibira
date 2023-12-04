(ns app.osmosi.noted.api.core
  (:require [app.osmosi.noted.db.core :as db]
            [app.osmosi.ibira.util.core :refer [form-decode]]))

(defn add-note [request]
  (let [data (form-decode (slurp (:body request)))]
    (db/add-note (get data "text"))
    {:status 201
     :headers {"content-type" "text/html"}
     :body "OK"}))

(ns app.osmosi.noted.core
  (:require [app.osmosi.ibira.elements :refer :all]
            [app.osmosi.ibira.store :refer [watch register-store combine-reducers dispatch] :as store]
            [app.osmosi.ibira.middleware :refer [ibira-store-middleware]]
            [app.osmosi.noted.pages :as pages]
            [app.osmosi.noted.reducers :as reducers]
            [app.osmosi.noted.api.core :as api]
            [org.httpkit.server :as hk-server]
            [clojure.string :as s]))

(register-store (combine-reducers {:current-tab reducers/current-tab}))

(defn static [uri] 
  (let [full-path (s/replace uri #"/public" "./public")]
    (clojure.java.io/file full-path)))

(def pages {"/" #(pages/index %)})

(defn- begins-with? [substr s]
  (clojure.string/starts-with? s substr))

(defn- handle-api [request]
  (condp = (:uri request)
    "/api/add-note" (api/add-note request)
    {:status 404
     :body "Not Found"}))

(defn page-handler [routes request]
  (let [uri (:uri request)
        page (get routes uri)
        headers {"content-type" "text/html"}]
    (condp begins-with? uri
      "/public/" {:status 200 :body (static uri)}
      "/api/" (handle-api request)
      (if page
        {:status 200
         :headers headers
         :session {:storeid "1234"}
         :body (str "<!doctype html>\n" (page request))}
        {:status 404
         :headers headers
         :body "Not Found"}))))

(defn start
  ([] (start (fn [h] (fn [r] (h r)))))
  ([middleware]
   (hk-server/run-server (-> (partial page-handler pages)
                             ibira-store-middleware
                             middleware) {:port 8085})))

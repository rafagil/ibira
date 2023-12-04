(ns shopping-list.core
  (:require [app.osmosi.ibira.middleware :refer [ibira-store-middleware]]
            [shopping-list.reducers.core :as reducers]
            [shopping-list.pages.list]
            [clojure.string :as s]
            [app.osmosi.ibira.store :include-macros true])
  (:gen-class))

(def routes {"/list" #(shopping-list.pages.list/render-js %)})

(defn handler [request]
  (let [page (get routes (:uri request))
        headers {"content-type" "text/html"}]
    (if page
      {:status 200
       :headers headers
       :body (page request)}
      {:status 404
       :headers headers
       :body "Not Found"})))

(reducers/register)

(defn- parse-request [worker-req]
  (->> (js->clj worker-req)
       (map (fn [[k v]] [(keyword k) v])) ;; can't use keywordize here since it's recursive
       flatten
       (apply hash-map)))

(defn ^:export process-request [request]
  (clj->js ((ibira-store-middleware handler) (parse-request request))))

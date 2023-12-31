(ns app.osmosi.ibira.middleware
  (:require [app.osmosi.ibira.store :as store]
            [clojure.walk :refer [keywordize-keys]]
            [app.osmosi.ibira.util.core :refer [form-decode]]))

(defn- begins-with? [substr s]
  (clojure.string/starts-with? s substr))

(defn- parse-request-params [request]
  (let [content-type (get-in request [:headers "content-type"])
        body (:body request)]
    (when (and (not (empty? body))
               (= content-type "application/x-www-form-urlencoded"))
      (->> body
           form-decode
           keywordize-keys))))

(defn- handle-store-updates [handler]
  (fn [request]
    (condp begins-with? (:uri request)
      "/store-updates"
      (let [uri (:uri request)
            fn-key (subs uri (inc (clojure.string/last-index-of uri "/")))
            func (store/get-fn fn-key)]
        {:status 200
         :headers {"content-type" "text/html"}
         :body (func)})
      "/dispatch"
      (let [uri (:uri request)
            action-id (subs uri (inc (clojure.string/last-index-of uri "/")))
            act (store/read-action action-id)
            actions (if (vector? act) act [act])]
        (doseq [action actions]
          (if (:body request)
            (store/dispatch (assoc action :request-params (parse-request-params request)))
            (store/dispatch action)))
        {:status 200 :body ""})
      (handler request))))

(defn ibira-store-middleware [handler]
  (fn [request]
    (let [original-state (store/get-state)
          response ((handle-store-updates handler) request)
          update-headers (store/get-update-headers original-state)]
      (if update-headers
        (assoc response :headers (assoc (:headers response) "HX-Trigger" update-headers))
        response))))


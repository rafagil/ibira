(ns app.osmosi.ibira.middleware
  (:require [app.osmosi.ibira.store :as store]))

(defn- begins-with? [substr s]
  (clojure.string/starts-with? s substr))

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
            action (store/read-action action-id)]
        (store/dispatch action)
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


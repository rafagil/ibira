(ns app.osmosi.ibira.middleware
  (:require [app.osmosi.ibira.store :as store]))

(defn- begins-with? [substr s]
  (clojure.string/starts-with? s substr))

(defn- parse-request-params [request]
  (let [content-type (get-in request [:headers "content-type"])
        body (slurp (:body request))]
    (when (and (not (empty? body))
               (= content-type "application/x-www-form-urlencoded"))
      (->> (clojure.string/split body #"&")
           (map #(clojure.string/split % #"="))
           flatten
           (apply hash-map)))))

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
          (if (and (:body request)
                   (= (type action) clojure.lang.PersistentArrayMap))
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


(ns app.osmosi.ibira.middleware
  (:require [app.osmosi.ibira.store :as store]
            [clojure.walk :refer [keywordize-keys]]
            [app.osmosi.ibira.util.core :refer [form-decode]]
            [clojure.string :as s]))

(defn- begins-with? [substr s]
  (s/starts-with? s substr))

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
            fn-key (last (s/split uri #"/"))
            func (store/get-fn fn-key)]
        {:status 200
         :headers {"content-type" "text/html"}
         :body (func)})
      "/dispatch"
      (let [uri (:uri request)
            [store-name action-id] (take-last 2 (s/split uri #"/"))
            act (store/read-action action-id)
            actions (if (vector? act) act [act])]
        (doseq [action actions]
          (if (:body request)
            (store/dispatch store-name (assoc action :request-params (parse-request-params request)))
            (store/dispatch store-name action)))
        {:status 200 :body ""})
      (handler request))))

(defn ibira-store-middleware
  ([handler] (ibira-store-middleware "default" handler))
  ([store-name handler]
   (fn [request]
     (let [original-state (store/get-state store-name)
           response ((handle-store-updates handler) request)
           update-headers (store/get-update-headers store-name original-state)]
       (if update-headers
         (assoc response :headers (assoc (:headers response) "HX-Trigger" update-headers))
         response)))))

(defn- generate-session-id []
  (str "sid_" (clojure.string/replace (random-uuid) "-" "" )))

(defn session-store-name [request]
  (or (get-in request [:session :store-name]) (generate-session-id)))

;; Requires compatible session ring middleware
(defn ibira-session-middleware [handler session-reducer]
  (fn [request]
    (let [store-name (session-store-name request)]
      (if (not (store/is-store-registered? store-name))
        (do
          (store/register-store store-name session-reducer)
          (let [new-handler (ibira-store-middleware store-name handler)
                new-session (assoc (:session request) :store-name store-name)
                response (new-handler (assoc request :session new-session))]
            (assoc response :session new-session)))
        ((ibira-store-middleware store-name handler) request)))))

;; Need a gc to clear the session stores;
;; We can save the creation/access timestamp and update on each request.
;; non used stores should be removed

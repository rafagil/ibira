(ns app.osmosi.ibira.middleware
  (:require [app.osmosi.ibira.store :as store]))

;; (defn updated-headers-middleware [handler] 
;;   (fn [request]
;;     (if (:original-store request)
;;       (let [response (handler request)
;;             headers (assoc (:headers response) "HX-Trigger" (store/get-update-headers (:original-store request)))]
;;         (assoc response :headers headers))
;;       (handler request))))

;; (defn store-changes-middleware [handler]
;;   (fn [request]
;;     (handler (assoc request :original-store (store/get-state)))))

(defn monitor-store-changes [handler]
  (fn [request]
    (let [original-state (store/get-state)
          response (handler request)
          update-headers (store/get-update-headers original-state)]
      (if update-headers
        (assoc response :headers (assoc (:headers response) "HX-Trigger" update-headers))
        response))))

(defn handle-store-updates [handler]
  (fn [request]
    (if (clojure.string/starts-with? (:uri request) "/store-updates")
      (let [uri (:uri request)
            fn-key (subs uri (inc (clojure.string/last-index-of uri "/")))
            func (store/get-fn fn-key)]
        {:status 200
         :headers {"content-type" "text/html"}
         :body (func)})
      (handler request))))

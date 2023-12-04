(ns core
  (:require [app.osmosi.ibira.middleware :refer [ibira-store-middleware]]
            [org.httpkit.server :as hk-server]
            [reducers.core :as reducers]
            [pages.list]
            [clojure.string :as s])
  (:gen-class))

(def routes {"/" #(pages.list/render %)})

(defn images [uri]
  (let [full-path (s/replace uri #"/public" "./public")]
    (clojure.java.io/file full-path)))

(defn handler [request]
  (if (s/starts-with? (:uri request) "/public")
    {:status 200
     :headers {"content-type" "image/png"}
     :body (images (:uri request))}
    (let [page (get routes (:uri request))
          headers {"content-type" "text/html"}]
      (if page
        {:status 200
         :headers headers
         :body (str "<!DOCTYPE html>\n" (page request))}
        {:status 404
         :headers headers
         :body "Not Found"}))))

(reducers/register)

(defn- body-slurp [handler]
  (fn [request]
    (if (:body request)
      (handler (assoc request :body (slurp (:body request))))
      (handler request))))

(defn -main [& args]
  (println "Running on port 2342")
  (hk-server/run-server (-> #(handler %)
                            ibira-store-middleware
                            body-slurp) {:port 2342}))

;; REPL only
;; (def stop-http
;;   (hk-server/run-server (-> #(handler %)
;;                             ibira-store-middleware) {:port 2342}))


;; Fazer um combine-reducer especial que guarda as informacoes no banco e gerencia tudo via token
;; A ideia eh ser um HoF que automagicamente gerencia varias stores baseadas no token
;; Precisa pensar como o dispatch vai passar o token nesse caso

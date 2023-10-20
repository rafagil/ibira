(ns htmx
  (:require [elements :refer :all]
            [org.httpkit.server :as hk-server]
            [clojure.string :as s]
            [clojure.java.shell :refer [sh]]))

(defn index [_]
  ((html {:lang "en-US"}
     (head
      (title "RipGrep WebUI")
      (script {:src "https://unpkg.com/htmx.org@1.9.6"
               :crossorigin "anonymous"}))
     (body
       ;; (with-store [user :user-info] ;; automagicamente usa o store/store namespace
       ;;   (div (-> user :name))) ;;// SSE? socket? (tornar opcional)
       ;; (div "Normal")
       (form {:hx-trigger "submit"
              :hx-target "#results-div"
              :hx-swap "outerHTML"
              :hx-post "/results"}
             (input {:type 
                     :name "term"
                     :placeholder "Search using RipGrep on ~"})
             (button {:type "submit"} "Search"))
       (div {:id "results-div"})
       ;; (button {:on-click #(store/dispatch {:type "EITA_ACTION"})} "Hello") ;; Esses eventos virao via HTTP. Precisa imaginar como :)
       ;; (form {:on-submit #(handle-submit)})
       )) "root"))

(defn results [req]
  (let [body (slurp (:body req))
        term (second (s/split body #"="))
        out (sh "rg" term "/home/rafa" )
        lines (s/split (:out out) #"\n")]
    ((ul {:id "results-div"}
         (plain-text (reduce str (map #((li (plain-text %)) "ha") lines)))) "bleh")))

(def pages {"/" #(index %)
            "/results" #(results %)})

(defn page-handler [routes request]
  (let [page (get routes (:uri request))]
    (if page
      {:status 200
       :headers {"content-type" "text/html"}
       :body (page request)}
      {:status 404
       :headers {"content-type" "text/html"}
       :body "Not Found"})))

;; (http/start 8082 (partial page-handler pages))

(def stop-http (hk-server/run-server (partial page-handler pages) {:port 8082}))


;; Can I use this only as an htmx template? Absolutelly!

;;TODO:
;;OK Page handler
;;   on-* events (precisa tratar eles de maneira diferente, preferencialmente via macro)
;;OK transformar o create-element em macro
;;   adicionar a store (copiar uma que ja funciona ou basear na do Java/heater)
;;   criar a macro with-store
;; remover os parent-id (desnecessario com a with-store/watch macro)
;; Fazer o map funcionar

(ns htmx
  (:require [elements :refer :all]
            [store :refer [watch]]
            [org.httpkit.server :as hk-server]
            [clojure.string :as s]
            [clojure.java.shell :refer [sh]]))

(def the-count (atom 0))
(def current-fn (atom nil))

;; Essa funcao vai ser extraida da macro.
;; E sera chamada imediatamente passando o parametro da store
(defn dentro-do-with-store [param]
  (reset! current-fn dentro-do-with-store)
  (div param))

(defn index [_]
  (html {:lang "en-US"}
     (head
      (title "RipGrep WebUI")
      (script {:src "https://unpkg.com/htmx.org@1.9.6"
               :crossorigin "anonymous"}))
     (link {:href "/public/tail.css" :rel "stylesheet"})
     (body
       ;; (with-store [user :user-info] ;; automagicamente usa o store/store namespace
       ;;   (div (-> user :name))) ;;// SSE? socket? (tornar opcional) (Htmx headers!)
       ;; (div "Normal")
       (h1 {:class "text-3xl"} "RipGrep")
       ;; Esse eh o with-store:
       (watch [users :users]
         (div (-> users first :name)))
       (span {:hx-trigger "onNomeDaChaveUpdate from:body"
              :hx-swap "innerHTML"
              :hx-get "/partial"} ;; ?funcId=idDafunc que ficara num hashtable global
             (dentro-do-with-store "Vai Mano"))
       ;; Fim da macro
       (div {:class "flex flex-col items-stretch flex-wrap items-center shadow-lg rounded-xl max-w-lg mx-auto p-6"}
         (form {:hx-trigger "submit"
                :hx-target "#results-div"
                :hx-swap "outerHTML"
                :hx-post "/results"
                :class "flex grow gap-x-2"}
               (input {:type "text"
                       :name "term"
                       :class "grow"
                       :placeholder "Search using RipGrep on ~"})
               (button {:type "submit" :class "bg-sky-500 hover:bg-sky-700 rounded-full text-sm text-white font-semibold px-5 py-2"} "Search"))
         (div {:id "result-count"
               :hx-trigger "onCount from:body"
               :hx-get "/result-count"})
         (div {:id "results-div"}))
       (button {:type "button"
                :hx-trigger "click"
                :hx-post "/trigger/update-users"})
       ;; (button {:on-click #(store/dispatch {:type "EITA_ACTION"})} "Hello") ;; Esses eventos virao via HTTP. Precisa imaginar como :)
       ;; (form {:on-submit #(handle-submit)})
       )))

(defn- highlight [term line]
  (s/replace line term (strong term)))

(defn results [req]
  (let [body (slurp (:body req))
        term (second (s/split body #"="))
        out (sh "rg" term "/home/rafa" )
        lines (s/split (:out out) #"\n")]
    (reset! the-count (count lines))
    (div {:id "results-div"}
      (div "Results")
      (ul
        (->> lines
             (map (partial highlight term))
             (map #(li %)))))))

(defn get-count [_] (div @the-count))

(defn static [_] (slurp "./public/tail.css"))

(defn auto-partial [req]
  ;; Aqui precisa mandar o header "onNomeDaChaveUpdate"
  (@current-fn "vish"))

(def pages {"/" #(index %)
            "/results" #(results %)
            "/result-count" #(get-count %)
            "/partial" #(auto-partial %)
            "/public/tail.css" #(static %)})

(defn meu-middleware [handler] ;; Esse Middleware vai ser responsavel por adicionar os headers necessarios para os updates
  (fn [request]
    (if (= (:uri request) "/tapreula")
      {:status 200
        :headers {"content-type" "text/html"}
        :body "Mano, interceptei"}
      (handler request))))

;; Fluxo da coisa toda:
;; Macro cria uma funcao com o conteudo dela e salva ela usando uma chave aleatoria.
;; A macro entao executa a funcao passando a store atual e embala em um span com os htmx da vida
;; A chave eh usada no hx-get (ex: /store-update/chave-aleatoria)
;; O evento hx-trigger eh o nome da chave (ou chaves) da store (ex. current-user)
;; Teremos entao dois middlewares:
;; 1 que intercepta as chamadas /store-update/chave-aleatoria e executa a funcao usando a chave passando a store como parametro
;; 2 que monitora a store por mudancas e adiciona os headers necessarios onde houveram updates
;; Testar isso tudo usando memoria mesmo e depois tentar persistir somente as actions
;; na memoria, fazendo com que precise executar ela de novo se fizer um refresh na pagina
;; Futuro: Criar um middleware para actions: POST /actions/nome-da-action {body em edn}
;; Aih pode chamar usando :dispatch {:on :evento /*htmx, ex click*/ :type "mark-read" :params {:param 1 "a"})

(defn page-handler [routes request]
  (let [page (get routes (:uri request))
        headers {"content-type" "text/html"}
        headers (if (= (:uri request) "/results")
                  (assoc headers "HX-Trigger" "onCount,onNomeDaChaveUpdate")
                  headers)]
    (if page
      {:status 200
       :headers headers
       :session {:storeid "1234"}
       :body (page request)}
      {:status 404
       :headers headers
       :body "Not Found"})))

;; (http/start 8082 (partial page-handler pages))

(def stop-http (hk-server/run-server (-> (partial page-handler pages)
                                         (meu-middleware)) {:port 8082}))

;; Can I use this only as an htmx template? Absolutelly!

;;TODO:
;;OK Page handler
;;   on-* events (precisa tratar eles de maneira diferente, preferencialmente via macro)
;;OK transformar o create-element em macro
;;   adicionar a store (copiar uma que ja funciona ou basear na do Java/heater)
;;   criar a macro with-store
;; remover os parent-id (desnecessario com a with-store/watch macro)
;; Fazer o map funcionar
;; Mosaic Presenter (le todas imagens da pasta e faz uma apresentacao aleatoria de 2 em 2 imagens)
;; Usar o hx-select pra mandar uma resposta só que atualiza todos os componentes visiveis
;; Assim: quando usar o with-store, marca o elemento com hx-select e um id.
;; Aí na resposta, manda esse elemento sempre junto caso exista
;; Precisa testar: O que acontece quando nao vai o id? como atualizar o resto sem o watched

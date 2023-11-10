(ns htmx
  (:require [app.osmosi.ibira.elements :refer :all]
            [app.osmosi.ibira.store :refer [watch register-store combine-reducers dispatch] :as store]
            [org.httpkit.server :as hk-server]
            [clojure.string :as s]
            [clojure.java.shell :refer [sh]]))

(defn ripgrep [term]
  (let [out (sh "rg" term "/home/rafa" )]
    (s/split (:out out) #"\n")))

(defn count-reducer [state action]
  (if (= (:type action "UPDATE_COUNTER"))
    (:count action)
    0))
(defn results-reducer [state action]
  (if (= (:type action) "SEARCH")
    {:data (ripgrep (:term action)) :term (:term action)}
    {:data [] :term ""}))

(register-store (combine-reducers {:count count-reducer
                                   :results results-reducer}))

(defn- highlight [term line]
  (s/replace line term (strong term)))

(defn index [_]
  (html {:lang "en-US"}
     (head
      (title "RipGrep WebUI")
      (script {:src "https://unpkg.com/htmx.org@1.9.6"
               :crossorigin "anonymous"}))
     (link {:href "/public/tail.css" :rel "stylesheet"})
     (body
       (h1 {:class "text-3xl"} "RipGrep WebUI")
       (watch [results :results]
         (span (str "The count is: " (count (:data results)))))
       (div {:class "flex flex-col items-stretch flex-wrap items-center shadow-lg rounded-xl max-w-lg mx-auto p-6"}
            (form {:hx-trigger "submit"
                   :hx-swap "none"
                   :hx-post "/results"
                   :class "flex grow gap-x-2"}
                  (input {:type "text"
                          :name "term"
                          :class "grow"
                          :placeholder "Search using RipGrep on ~"})
                  (button {:type "submit" :class "bg-sky-500 hover:bg-sky-700 rounded-full text-sm text-white font-semibold px-5 py-2"} "Search"))
            (watch [results :results]
              (let [data (:data results)
                    term (:term results)
                    _ (println results)]
                (div {:id "results-div"}
                     (div (str "Found " (count data) " Results"))
                     (ul
                       (->> data
                            (map (partial highlight term))
                            (map #(li %)))))))))))

(defn results [req]
  (let [body (slurp (:body req))
        term (second (s/split body #"="))]
    (dispatch {:type "SEARCH" :term term})
    ""))

(defn static [_] (slurp "./public/tail.css"))
(def pages {"/" #(index %)
            "/results" #(results %)
            "/public/tail.css" #(static %)})

(defn updated-headers-middleware [handler] 
  (fn [request]
    (if (:original-store request)
      (let [response (handler request)
            headers (assoc (:headers response) "HX-Trigger" (store/get-update-headers (:original-store request)))]
        (assoc response :headers headers))
      (handler request))))

(defn store-changes-middleware [handler]
  (fn [request]
    (handler (assoc request :original-store (store/get-state)))))

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
;; Agora falta criar componentes htmx hx-form, hx-button, etc que fazem todo
;; o malabarismo para fazer o dispatch automagico

(defn page-handler [routes request]
  (let [page (get routes (:uri request))
        headers {"content-type" "text/html"}]
    (if page
      {:status 200
       :headers headers
       :session {:storeid "1234"}
       :body (page request)}
      {:status 404
       :headers headers
       :body "Not Found"})))

(defn handle-store-updates [handler]
  (fn [request]
    (if (s/starts-with? (:uri request) "/store-updates")
      (let [uri (:uri request)
            fn-key (subs uri (inc (s/last-index-of uri "/")))
            func (store/get-fn fn-key)]
        {:status 200
         :headers {"content-type" "text/html"}
         :body (func)})
      (handler request))))

  (def stop-http (hk-server/run-server (-> (partial page-handler pages)
                                           handle-store-updates
                                           updated-headers-middleware
                                           store-changes-middleware ) {:port 8082}))

;; Can I use this only as an htmx template? Absolutelly!

;;TODO:
;;OK Page handler
;;   on-* events (precisa tratar eles de maneira diferente, preferencialmente via macro)
;;OK transformar o create-element em macro
;;   adicionar a store (copiar uma que ja funciona ou basear na do Java/heater)
;;OK   criar a macro with-store
;;OK remover os parent-id (desnecessario com a with-store/watch macro)
;;OK Fazer o map funcionar
;; Mosaic Presenter (le todas imagens da pasta e faz uma apresentacao aleatoria de 2 em 2 imagens)
;; Usar o hx-select pra mandar uma resposta só que atualiza todos os componentes visiveis
;; Assim: quando usar o with-store, marca o elemento com hx-select e um id.
;; Aí na resposta, manda esse elemento sempre junto caso exista
;; Precisa testar: O que acontece quando nao vai o id? como atualizar o resto sem o watched

  ;; PAREI AQUI: Fazer agora o results do ripgrep vir da store
  ;; e manter o valor do input? (pode ser feito via um eventual hx-input que sabe se eh um re-render ou se eh o primeiro render)

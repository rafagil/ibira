(ns app.osmosi.ibira.store)

(def store (atom {}))

(defn combine-reducers 
  ([reducer-map] (combine-reducers "default" reducer-map)) 
  ([store-name reducer-map]
   (fn [state action]
     (apply conj {} (map (fn [[store-key reducer]] {store-key (reducer (get state store-key) action)}) reducer-map)))))

;; How it's going to work:
(defn register-store
  ([reducer] (register-store "default" reducer))
  ([name reducer]
   (swap! store assoc-in [name] {:state (reducer nil {:type "INIT"})
                                 :reducer reducer})))

(defn dispatch 
  ([action] (dispatch "default" action))
  ([store-name action]
   (let [old-store (@store store-name)
         new-state ((:reducer old-store) (:state old-store) action)]
     (swap! store assoc-in [store-name] {:state new-state :reducer (:reducer old-store)}))))

(defn get-state
  ([] (get-state "default"))
  ([name] (get-in @store [name :state])))

(defn get-update-headers
  ([original-state] (get-update-headers "default" original-state))
  ([store-name original-state]
   (->> (get-state)
        (filter (fn [[k v]] (not= v (get original-state k)) ))
        (map first)
        (map #(str "on-" store-name "-" (name %) "-update"))
        (clojure.string/join ","))))

;; Macro:
;; (watch [users :users] (div (-> user :name)))
;; expand:
;; (span {:hx-trigger "on-users-update from:body" :hx-swap "innerHTML" :hx-get "/store-updates/users"} (let [users (:users @store)] (div (-> user :name))))
;; TODO: Multiple store prop updates 

(def fn-map (atom {}))
(defn store-fn [fn-key func]
  (swap! fn-map assoc fn-key func))

(defn get-fn [fn-key] (get @fn-map fn-key))

(defn build-watch-macro [store-name watch-vector & body]
  (let [store-key (second watch-vector)
        fn-hash (str (hash body) (hash watch-vector))
        var-name (first watch-vector)
        func-sym (gensym "func")]
    (list 'app.osmosi.ibira.elements/element-with-props-and-children
          "span"
          (hash-map :hx-trigger (str "on-" store-name "-" (name store-key) "-update from:body")
                    :hx-swap "innerHTML"
                    :hx-get (str "/store-updates/" fn-hash))
          (list 'let (vector func-sym (list 'fn '[] (apply list 'let (vector var-name (list 'get (list 'app.osmosi.ibira.store/get-state store-name) store-key)) body)))
                (list 'app.osmosi.ibira.store/store-fn fn-hash func-sym)
                (list func-sym)))))

(defmacro watch-store [store-name watch-vector & body]
  (apply build-watch-macro store-name watch-vector body))

(defmacro watch [watch-vector & body]
  (apply build-watch-macro "default" watch-vector body))

;; Reationale:
;; span eh criada com trigger = on-default-users-update
;; todo request gera um headers contendo todas as chaves que tiveram alteracao
;; no evento, o htmx vai fazer um request de update passando o id da funcao, que sera executada e retornara o partial html

;; (macroexpand '(watch [users :users] (div (-> users first :name))))
;; (watch [users :users] (elements/div (-> users first :name)))

;; (defn users-reducer [state action]
;;   (if (= (:type action) "INIT")
;;     [{:id 1 :name "Rafael"}
;;      {:id 2 :name "Daiana"}]
;;     (if (= (:type action) "UPDATE")
;;       "updateado"
;;       state)))


;; (register-store (combine-reducers {:users users-reducer}))
;; (get-update-headers (dispatch {:type "UPDATE"}))
;; ;; (dispatch {:type "INIT"})


;; (defn build-watch-macro [store-name watch-vector & body]
;;   (let [store-key (second watch-vector)
;;         store-key-name (str (hash body) (hash watch-vector))
;;         var-name (first watch-vector)
;;         func-sym (gensym "func")]
;;     (list 'elements/element-with-props-and-children
;;           "span"
;;           (hash-map :hx-trigger (str "on-" store-key-name "-update from:body")
;;                     :hx-swap "innerHTML"
;;                     :hx-get (str "/store-updates/" store-key-name))
;;           ;; na vdd este 'let precise estar na vdd dentro da funcao func-sym (pq senao nao atualiza quando executar a funcao novamente)
;;           (list 'let (vector var-name (list 'get '(store/get-state store-name) store-key)
;;                              func-sym (apply list 'fn '[] body))
;;                 (list 'store/store-fn store-key-name func-sym)
;;                 (list func-sym)))))

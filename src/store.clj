(ns store)

(defn users-reducer [state action]
  (if (= (:type action) "list")
    [{:id 1 :name "Rafael"}
     {:id 2 :name "Daiana"}]
    state))

(def store (atom {:users (users-reducer [] {:type "list"})}))

;; Macro:
;; (watch [users :users] (div (-> user :name)))
;; expand:
;; (span {:hx-trigger "on-users-update from:body" :hx-swap "innerHTML" :hx-get "/store-updates/users"} (let [users (:users @store)] (div (-> user :name))))
;; TODO: Multiple store prop updates 

(def fn-map (atom {}))
(defn store-fn [store-key func]
  (reset! fn-map (assoc @fn-map store-key func)))

(defmacro watch [watch-vector & body]
  (let [store-key (second watch-vector)
        store-key-name (str (hash body) (hash watch-vector))
        var-name (first watch-vector)
        func-sym (gensym "func")]
    (list 'elements/element-with-props-and-children
          "span"
          (hash-map :hx-trigger (str "on-" store-key-name "-update from:body")
                    :hx-swap "innerHTML"
                    :hx-get (str "/store-updates/" store-key-name))
          (list 'let (vector var-name (list store-key '@store/store)
                             func-sym (apply list 'fn '[] body))
                (list 'store/store-fn store-key-name func-sym)
                (list func-sym)))))

;; (macroexpand '(watch [users :users] (div (-> users first :name))))
;; (watch [users :users] (elements/div (-> users first :name)))


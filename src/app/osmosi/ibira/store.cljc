(ns app.osmosi.ibira.store
  (:require [clojure.test]
            [clojure.string]))

(def store (atom {}))

(defn combine-reducers 
  ([reducer-map] (combine-reducers "default" reducer-map)) 
  ([store-name reducer-map]
   (fn [state action]
     (apply conj {} (map (fn [[store-key reducer]] {store-key (reducer (get state store-key) action)}) reducer-map)))))

(defn register-store
  ([reducer] (register-store "default" reducer))
  ([name reducer] (register-store name reducer (reducer nil {:type "INIT"})))
  ([name reducer initial-state]
   (swap! store assoc-in [name] {:state initial-state
                                 :reducer reducer})))
(defn unregister-store [name]
  (swap! store dissoc name))

(defn dispatch 
  ([action] (dispatch "default" action))
  ([store-name action]
   (let [old-store (@store store-name)
         new-state ((:reducer old-store) (:state old-store) action)]
     (swap! store assoc-in [store-name] {:state new-state :reducer (:reducer old-store)}))))

(defn get-state
  ([] (get-state "default"))
  ([name] (get-in @store [name :state])))

(defn is-store-registered? [name]
  (contains? @store name))

(defn get-update-headers
  ([original-state] (get-update-headers "default" original-state))
  ([store-name original-state]
   (->> (get-state store-name)
        (filter (fn [[k v]] (not= v (get original-state k)) ))
        (map first)
        (map #(str "on-" store-name "-" (name %) "-update"))
        (clojure.string/join ","))))

(def fn-map (atom {}))
(defn store-fn [fn-key func]
  (swap! fn-map assoc fn-key func))

(defn get-fn [fn-key] (get @fn-map fn-key))

(defn- gen-trigger [store-name watch-kv]
  (let [values (mapv second watch-kv)
        map-fn (list 'fn ['i] (list 'str "on-" store-name "-" (list 'name 'i) "-update from:body"))]
    (list 'clojure.string/join "," (list 'map map-fn values))))

(defn- gen-let [store-name watch-kv]
  (->> watch-kv
       (map (fn [[k v]] [k (list 'get (list 'app.osmosi.ibira.store/get-state store-name) v)] ) )
       (apply concat)
       vec
       ))

(defn build-watch-macro [store-name watch-vector component props & body]
  (let [watch-kv (apply hash-map watch-vector)
        fn-hash (str (hash body) (hash watch-vector))
        func-sym (gensym "func")]
    (list component
          (conj {:hx-trigger (gen-trigger store-name watch-kv)
                 :hx-swap "innerHTML"
                 :hx-get (str "/store-updates/" fn-hash)}
                props)
          (list 'let [func-sym (list 'fn '[] (apply list 'let (gen-let store-name watch-kv) body))]
                (list 'app.osmosi.ibira.store/store-fn fn-hash func-sym)
                (list func-sym)))))

(defn- span-watch
  ([watch-vector body] (span-watch watch-vector {} body))
  ([watch-vector props body]
   (let [store-name (or (:store props) "default")]
     (build-watch-macro store-name watch-vector 'app.osmosi.ibira.elements/span props body))))

(defn- component-watch
  ([component watch-vector body] (component-watch component watch-vector {} body))
  ([component watch-vector props body]
   (let [store-name (or (:store props) "default")]
     (build-watch-macro store-name watch-vector component props body))))

(defmacro watch [& args]
  (let [expected-signatures {[clojure.lang.PersistentVector clojure.lang.PersistentList] span-watch
                             [clojure.lang.PersistentVector clojure.lang.PersistentArrayMap clojure.lang.PersistentList] span-watch
                             [clojure.lang.Symbol clojure.lang.PersistentVector clojure.lang.PersistentList] component-watch
                             [clojure.lang.Symbol clojure.lang.PersistentVector clojure.lang.PersistentArrayMap clojure.lang.PersistentList] component-watch}
        signature (mapv type args)
        match (expected-signatures signature)]
    (if match (apply match args) (throw (Exception. (str "Invalid Signature: " signature))))))

(def action-map (atom {}))
(defn read-action [id]
  (get @action-map id))

(defmacro action [component props the-action & children]
  (let [action-id-sym (gensym "action-id")
        action-props-sym (gensym "action-props")
        store-name (or (:store props) "default")]
    (list 'let [action-id-sym (list 'str (list 'hash the-action))
                action-props-sym (dissoc props :store)]
          (list 'swap! 'app.osmosi.ibira.store/action-map 'assoc action-id-sym the-action)
          (if (clojure.test/function? component)
            (apply list component (list 'conj {:hx-post (list 'str "/dispatcher/" store-name "/" action-id-sym)
                                               :hx-swap "none"} action-props-sym) children)
            (apply list 'app.osmosi.ibira.elements/element-with-props-and-children (name component)
                   (list 'conj {:hx-post (list 'str "/dispatcher/" store-name "/" action-id-sym)
                                :hx-swap "none"} action-props-sym) children)))))


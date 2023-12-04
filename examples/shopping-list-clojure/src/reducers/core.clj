(ns reducers.core
  (:require 
   [app.osmosi.ibira.store :refer [register-store combine-reducers] :as store]
   [reducers.items :as items]
   [reducers.add-form :as add-form]
   [clojure.string :as s])
  (:gen-class))

(def DB_FILE_NAME "./thestate.txt")

(defn- db-reducer [reducer-fn]
  (fn [state action]
    (let [new-state (reducer-fn state action)]
      (spit (clojure.java.io/file DB_FILE_NAME) (pr-str new-state))
      new-state)))

(defn- hydrate []
  (when (.exists (clojure.java.io/file DB_FILE_NAME))
    (read-string (slurp DB_FILE_NAME))))

(defn register []
  (let [initial-state (hydrate)
        combined-reducers (combine-reducers {:items items/reducer
                                             :add-form add-form/reducer})]
    (if initial-state
      (register-store "default" (db-reducer combined-reducers) initial-state)
      (register-store "default" (db-reducer combined-reducers)))))

;; Brainstorm:
;; (dispatch (db-insert item)) ;; o db-insert eh um action creator
;; O reducer ao receber a action, atualiza a lista de retorno fazendo um select
;; pagination funciona como um parametro no proprio reducer: {:items {:items [] :page 1 :count 100}}

(ns shopping-list.reducers.core
  (:require 
   [app.osmosi.ibira.store :refer [register-store combine-reducers] :as store]
   [shopping-list.reducers.items :as items]
   [shopping-list.reducers.add-form :as add-form]
   [clojure.string :as s])
  (:gen-class))

;; (def DB_FILE_NAME "./oestado.txt")

;; (defn- db-reducer [reducer-fn]
;;   (fn [state action]
;;     (let [new-state (reducer-fn state action)]
;;       (spit (clojure.java.io/file DB_FILE_NAME) (pr-str new-state))
;;       new-state)))

;; (defn- hydrate []
;;   (when (.exists (clojure.java.io/file DB_FILE_NAME))
;;     (read-string (slurp DB_FILE_NAME))))

(defn register []
  (register-store (combine-reducers {:items items/reducer
                                             :add-form add-form/reducer}))
  ;; (let [initial-state (hydrate)
  ;;       combined-reducers (combine-reducers {:items items/reducer
  ;;                                            :add-form add-form/reducer})]
  ;;   (if initial-state
  ;;     (register-store "default" (db-reducer combined-reducers) initial-state)
  ;;     (register-store "default" (db-reducer combined-reducers))))

  )

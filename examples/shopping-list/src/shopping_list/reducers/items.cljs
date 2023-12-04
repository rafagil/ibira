(ns shopping-list.reducers.items
  (:require [shopping-list.reducers.actions :as actions]))

(defn add [item]
  ;; (assoc item :id (str (java.util.UUID/randomUUID))))
  (assoc item :id (str (.randomUUID js/crypto))))

(defn delete [state item]
  (filterv #(not= (:id %) (:id item)) state))

(defn set-check [state action]
  (sort-by :checked
           (mapv #(if (= (:id %) (:id action)) (assoc % :checked (:checked action)) %) state)))

(defn reducer [state action]
  (condp = (:type action)
    "INIT" []
    actions/ADD_ITEM (apply conj [] (add (:request-params action)) state)
    actions/REMOVE_ITEM (delete state action)
    actions/SET_CHECK (set-check state action)
    state))

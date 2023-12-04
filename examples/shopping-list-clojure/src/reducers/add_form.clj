(ns reducers.add-form)

(defn reducer [state action]
  (condp = (:type action)
    "INIT" {:title ""}
    "ADD_ITEM" {:title ""}
    "UPDATE_ADD_FORM" (conj state (:request-params action))
    state))


(ns app.osmosi.noted.reducers
  (:require [app.osmosi.noted.actions :as actions]))

(defn current-tab [state action]
  (condp = (:type action)
    actions/INIT :add
    actions/SET_CURRENT_TAB (:tab action)
    :add))
  

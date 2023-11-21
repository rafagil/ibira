(ns app.osmosi.ibira.util.core
  (:require [clojure.string :as s]))

(defn form-decode [data]
  (->> (clojure.string/split data #"&")
       (map #(clojure.string/split % #"="))
       flatten
       (map #(js/decodeURIComponent %))
       (apply hash-map)))

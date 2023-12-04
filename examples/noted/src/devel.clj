(ns devel
  (:require [app.osmosi.noted.core :as core]
            [clojure-watch.core :refer [start-watch]]
            [org.httpkit.server :refer [send! with-channel on-close]]))

(def channels (atom {}))

(def stop-watch (start-watch [{:path "./src"
                               :event-types [:create :modify :delete]
                               :bootstrap (fn [_] (println "Watching"))
                               :callback (fn [event filename] (doseq [channel (keys @channels)]
                                                                (send! channel "Changed")))
                              :options {:recursive true}}]))

(defn watch-ws [handler]
  (fn [request]
    (if (= (:uri request) "/watch")
      (with-channel request channel
        (swap! channels assoc channel request)
        (on-close channel (fn [status]
                            (swap! channels dissoc channel))))
      (handler request))))


(def stop-http (core/start watch-ws))  

(defn stop []
  (stop-watch)
  (stop-http))
              

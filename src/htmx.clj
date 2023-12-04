(ns htmx
  (:require [app.osmosi.ibira.elements :refer :all]
            [app.osmosi.ibira.store :refer [watch register-store combine-reducers dispatch] :as store]
            [app.osmosi.ibira.middleware :refer [handle-store-updates monitor-store-changes]]
            [org.httpkit.server :as hk-server]
            [clojure.string :as s]
            [clojure.java.shell :refer [sh]]))

(defn ripgrep [term]
  (let [out (sh "rg" term "/home/rafa" )]
    (s/split (:out out) #"\n")))

(defn count-reducer [state action]
  (if (= (:type action "UPDATE_COUNTER"))
    (:count action)
    0))
(defn results-reducer [state action]
  (if (= (:type action) "SEARCH")
    {:data (ripgrep (:term action)) :term (:term action)}
    {:data [] :term ""}))

(register-store (combine-reducers {:count count-reducer
                                   :results results-reducer}))

(defn- highlight [term line]
  (s/replace line term (strong term)))

(defn index [_]
  (html {:lang "en-US"}
     (head
      (title "RipGrep WebUI")
      (script {:src "https://unpkg.com/htmx.org@1.9.6"
               :crossorigin "anonymous"}))
     (link {:href "/public/tail.css" :rel "stylesheet"})
     (body
       (h1 {:class "text-3xl"} "RipGrep WebUI")
       (watch [results :results]
         (span (str "The count is: " (count (:data results)))))
       (div {:class "flex flex-col items-stretch flex-wrap items-center shadow-lg rounded-xl max-w-lg mx-auto p-6"}
            (form {:hx-trigger "submit"
                   :hx-swap "none"
                   :hx-post "/results"
                   :class "flex grow gap-x-2"}
                  (input {:type "text"
                          :name "term"
                          :class "grow"
                          :placeholder "Search using RipGrep on ~"})
                  (button {:type "submit" :class "bg-sky-500 hover:bg-sky-700 rounded-full text-sm text-white font-semibold px-5 py-2"} "Search"))
            (watch [results :results]
              (let [data (:data results)
                    term (:term results)]
                (div {:id "results-div"}
                     (div (str "Found " (count data) " Results"))
                     (ul
                       (->> data
                            (map (partial highlight term))
                            (map #(li %)))))))))))

(defn results [req]
  (let [body (slurp (:body req))
        term (second (s/split body #"="))]
    (dispatch {:type "SEARCH" :term term})
    ""))

(defn static [_] (slurp "./public/tail.css"))
(def pages {"/" #(index %)
            "/results" #(results %)
            "/public/tail.css" #(static %)})

(defn page-handler [routes request]
  (let [page (get routes (:uri request))
        headers {"content-type" "text/html"}]
    (if page
      {:status 200
       :headers headers
       :session {:storeid "1234"}
       :body (page request)}
      {:status 404
       :headers headers
       :body "Not Found"})))

(defn -main [& args]
  (println "Running on port 8083")
  (hk-server/run-server (-> (partial page-handler pages)
                            ibira-store-middleware) {:port 8082}))

;;REPL only
;; (def stop-http
;;   (hk-server/run-server (-> (partial page-handler pages)
;;                             ibira-store-middleware) {:port 8082}))

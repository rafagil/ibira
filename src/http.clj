(ns http
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]))

(def running (atom false))
(def re-? (re-pattern (java.util.regex.Pattern/quote "?")))
(def new-line "\r\n")

(defn parse-query [full-path]
  (if (> (count full-path) 1)
    (let [query (str/split (full-path 1) #"&")]
      (->> (map #(str/split % #"=") query)
           (apply conj {})
           (keywordize-keys)))
    {}))

(defn read-body [reader]
  (loop [body ""]
    (let [ln (.readLine reader)]
      (println "Reading: " ln)
      (if (not= ln nil)
        (recur (str body ln))
        body))))

(defn parse-request [first-line reader]
  (let [parts (str/split first-line #" ")]
    (if (= (count parts) 3)
      (let [method (parts 0)
            full-path (str/split (parts 1) re-?)
            path (full-path 0)
            query-params (parse-query full-path)
            body (read-body reader)]
        (println body)
        {:path path
         :query-params query-params
         :method method})
      nil))
  )

(defn process-request [socket handler]
  (future
    (let [in (new java.io.BufferedReader (new java.io.InputStreamReader (.getInputStream socket)))
          out (new java.io.BufferedOutputStream (.getOutputStream socket))
          printer (new java.io.PrintStream out)
          first-line (.readLine in)]
      (when first-line
        (let [request (parse-request first-line in)]
            (try
              (if request
                (let [response (handler request)
                      output (str "HTTP/1.1 " (-> response :status :code) " " (-> response :status :description)
                                  new-line
                                  "Content-type: " (-> response :content-type)
                                  new-line
                                  new-line
                                  (-> response :body)
                                  new-line
                                  new-line)]
                  (.print printer output))
                (.print printer "HTTP/1.1 400 Bad Request\r\n\r\n"))
              (catch Exception e
                (.printStackTrace e)
                (when printer (.print printer "HTTP/1.1 500 Server Error\r\n\r\n")))
              (finally (.close printer))))))))

(defn example-handler [request]
  {:status {:code 200 :description "OK"}
   :content-type "text/html"
   :body (str "You sent " (:query-params request) " in the query params")})

(defn start[port handler]
  (let [server-socket (new java.net.ServerSocket port)]
    (println "Starting HTTP server on port " port)
    (reset! running true)
    (future
      (loop [socket (.accept server-socket)]
        (process-request socket handler)
        (if @running
          (recur (.accept server-socket))
          (.close server-socket)))
      (println "Stopped"))))

(defn stop []
  (reset! running false))

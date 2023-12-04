(ns app.osmosi.noted.pages
  (:require [app.osmosi.ibira.elements :refer :all]
            [app.osmosi.ibira.store :refer [watch action]]
            [app.osmosi.noted.actions :as actions]
            [app.osmosi.noted.db.core :as db])
  (:gen-class))

(defn- is-active? [active-tab key]
  (if (= active-tab key)
    "is-active"
    ""))

(defn add-tab []
  (action form {:hx-trigger "submit"
                :class "is-flex is-flex-direction-column"
                :hx-post "/api/add-note"
                "hx-on::after-request" "this.reset()"
                :hx-swap "none"} {:type actions/ADD_NOTE}
    (div {:class "field text-div"}
         (textarea {:name "text" :class "text-note textarea" :placeholder "Write anything here"}))
    (button {:type "submit" :class "button is-primary is-fullwidth" :hx-disabled-elt "this"} "Save")))

(defn plan-tab []
  (let [tasks (db/get-tasks)]
    (ul
      (map #(li (:tasks/text %)) tasks))))

(defn index [_]
  (html
    (head
      (title "Noted")
      (html-meta {:name "viewport" :content "width=device-width, initial-scale=1"})
      (link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css"})
      (link {:rel "stylesheet" :href "/public/index.css"})
      (script {:src "https://unpkg.com/htmx.org@1.9.6" :crossorigin "anonymous"})
      (script {:src "/public/index.js"}))
    (body
      (div
        (h1 {:class "title"} "Noted!")
        (watch [active-tab :current-tab] {:tag-name "div"}
               (div
                 (div {:class "tabs"}
                      (ul
                        (li {:class (is-active? active-tab :add)}
                            (action a {:hx-tigger "click"} {:type actions/SET_CURRENT_TAB :tab :add} "Add note"))
                        (li {:class (is-active? active-tab :plan)}
                            (action a {:hx-trigger "click"} {:type actions/SET_CURRENT_TAB :tab :plan} "Plan"))))
                 (if (= active-tab :plan)
                   (plan-tab)
                   (add-tab))))))))

;;(load "pages/index")

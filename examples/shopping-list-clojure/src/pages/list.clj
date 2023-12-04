(ns pages.list
  (:require [app.osmosi.ibira.elements :refer :all]
            [app.osmosi.ibira.store :refer [watch action]]
            [reducers.actions :as actions]))

(def stylesheet "
html {height:100%;
overscroll-behavior: none;}
body {
  overscroll-behavior: none;
  margin:0;padding:0;font-family:sans-serif;height:100%}
h1 { padding-left: 0.5rem }
.container {
  margin:0;
  padding:0;
  display:flex;
  flex-direction: column;
  height: 100%;
}

.list {
  margin: 0;
  padding: 0 1rem;
  flex: 1;
  overflow: auto;
}
.list li {
  padding-top: 1rem;
  padding-bottom: 1rem;
  border-top: 0.5px solid #dadada;
  list-style-type: none;
  display: flex;
}
.list li:last-child {
  border-bottom: 0.5px solid #dadada;
}
.list li span {
  flex: 1;
  padding-left: 1rem;
  font-size: large;
}
.list li .checkbox {
  width: 1rem;
}

.list li button {
  background-color: #fff;
  border: 0;
  cursor: pointer;
  color: #ff0000;
  padding: 0;
  font-size: larger;
}

.list li button:focus-visible {
  outline: none;
  border-bottom:1px solid grey;
}

.footer {
  background-color: #dadada;
  bottom: 0;
  width: 100%;
  display: flex;
  padding-bottom: 2rem;
}

.footer input {
  flex:1;
  margin: 0.5rem 1rem;
}

.footer form {
  margin: 0 1rem 0 0;
  display: flex;
  flex: 1;
}

.footer form input {
  flex: 1;
  border: none;
  border-radius: 5px;
  padding-left: 0.5rem;
  font-size: large;
}

.footer form input:focus-visible {
  border: none;
  border-radius: 5px;
  outline: none;
}
.footer form button {
  margin: 0.5rem 0;
  border: 0;
  border-radius: 5px;
  padding: 0 1rem;
  background-color: purple;
  color: white;
  cursor: pointer;
  font-weight: 700;
}
.footer form button:focus-visible {
  outline: none;
}
@media (max-width:550px) {
  .keyboard-visible {
    padding-bottom: 0;
  }
}
")

(defn- enable-pwa-ios []
  (list
   (html-meta {:name "apple-mobile-web-app-capable" :content "yes"})
   (html-meta {:name "apple-mobile-web-app-title" :content "Shopping List"})
   (html-meta {:name "apple-mobile-web-app-status-bar-style" :content "black-translucent"})
   (link {:rel "apple-touch-icon" :href "/public/app-icon.png"})))

(defn- list-item [item]
  (let [toggle-action {:type actions/SET_CHECK
                       :id (:id item)
                       :checked (not (:checked item))}]
    (li
      (action div {:hx-trigger "click" :class "checkbox"} toggle-action
        (if (:checked item) "&#9745;" "&#9744;"))
      (action span {:hx-trigger "click"
                    :style (if (:checked item) "text-decoration:line-through" "")}
          toggle-action
        (:title item))
      (action button {:type "button" :hx-confirm "Are you sure?"} {:type actions/REMOVE_ITEM :id (:id item)}
        "&CircleTimes;"))))

(defn render [_]
  (html {:lang "en_US"}
        (head
          (title "Shopping List")
          (html-meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"})
          (html-meta {:charset "UTF-8"})
          (enable-pwa-ios)
          (script {:src "https://unpkg.com/htmx.org@1.9.6" :crossorigin "anonymous"})
          (style stylesheet))
        (body
          (div {:class "container"}
               (h1 "Shopping List")
               (watch [items :items] {:tag-name "ul" :class "list" }
                      (map list-item items))
               (div {:class "footer"}
                    (action form {:hx-trigger "submit"
                                  "hx-on:submit" "document.querySelector('.list').scrollTo(0,0);"
                                  "hx-on::after-request" "this.reset()"}
                        {:type actions/ADD_ITEM}
                      (input {:placeholder "New Item"
                              :type "text"
                              :name "title"
                              :onfocus "document.querySelector('.footer').classList.add('keyboard-visible')"
                              :onblur "document.querySelector('.footer').classList.remove('keyboard-visible')"})
                      (button {:type "submit"} "+")))))))




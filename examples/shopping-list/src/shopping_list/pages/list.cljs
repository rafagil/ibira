(ns shopping-list.pages.list
  (:require [app.osmosi.ibira.elements
             :refer [html head input button div span script html-meta link li body title style h1 form]
             :include-macros true]
            [app.osmosi.ibira.store :refer [watch action] :include-macros true]
            [shopping-list.reducers.actions :as actions]))

;; TODO: Move this to a .css file:
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

(defn render-js [_]
  (span 
   (style stylesheet)
   (div {:class "container"}
        (h1 "Shopping List")
        (watch [items :items] {:tag-name "ul" :class "list" }
               (map list-item items))
        (div {:class "footer"}
             (action form {:hx-trigger "submit"
                           "hx-on::after-request" "this.reset();document.querySelector('.list').scrollTo(0,0);"}
                     {:type actions/ADD_ITEM}
                     (input {:placeholder "New Item"
                             :type "text"
                             :name "title"
                             :onfocus "document.querySelector('.footer').classList.add('keyboard-visible')"
                             :onblur "document.querySelector('.footer').classList.remove('keyboard-visible')"})
                     (button {:type "submit"} "+"))))))

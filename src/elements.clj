(ns elements)

(defn- parse-props [props]
  (clojure.string/trim
	 (reduce str (map #(str (name (first %)) "=\"" (second %) "\" ") props))))

(defn- process-children [children parent-id]
  (reduce str (map (fn [child] (child parent-id)) children)))

(defn- has-props [lst]
  (and lst (= (type (first lst)) clojure.lang.PersistentArrayMap)))

(defn element-empty [tag-name]
  (fn []
    (fn [parent-id]
      (str "<" tag-name " parent-id=\"" parent-id "\" />\n"))))

(defn script-element []
  (fn [props]
    (fn [_]
      (str "<script " (parse-props props) "></script>\n"))))

(defn element-with-props [tag-name]
  (fn [props]
    (fn [parent-id]
      (str "<" tag-name " parent-id=\"" parent-id "\" " (parse-props props) "/>\n"))))

(defn element-with-children [tag-name]
  (fn [children]
    (fn [parent-id]
      (let [my-id (str parent-id "-fi")]
        (str "<" tag-name " parent-id=\"" parent-id "\" my-id=\"" my-id "\">\n"
             (process-children children my-id)
             "</" tag-name ">\n")))))

(defn element-with-string-child [tag-name]
  (fn [children]
    (fn [parent-id]
      (let [my-id (str parent-id "-fi")]
        (str "<" tag-name " parent-id=\"" parent-id "\" my-id=\"" my-id "\">"
             children
             "</" tag-name ">\n")))))

(defn element-with-props-and-children [tag-name]
  (fn [props children]
    (fn [parent-id]
      (let [my-id (str parent-id "-fi")]
        (str "<" tag-name " parent-id=\"" parent-id "\" my-id=\"" my-id "\" "
             (parse-props props) ">\n"
             (process-children children my-id)
             "</" tag-name ">\n")))))

(defn element-with-props-and-string-child [tag-name]
  (fn [props children]
    (fn [parent-id]
      (let [my-id (str parent-id "-fi")]
        (str "<" tag-name " parent-id=\"" parent-id "\" my-id=\"" my-id "\" "
             (parse-props props) ">"
             children
             "</" tag-name ">\n")))))

;; (defn create-element [tag-name]
;;   (fn [& args]
;;     (if (has-props args)
;;         (if (empty? (rest args))
;;             ((element-with-props tag-name) (first args))
;;             (if (string? (second args))
;;               ((element-with-props-and-string-child tag-name) (first args) (second args))
;;               ((element-with-props-and-children tag-name) (first args) (rest args))))
;;         (if (empty? args)
;;             ((element-empty tag-name))
;;             (if (string? (first args))
;;                 ((element-with-string-child tag-name) (first args))
;;                 ((element-with-children tag-name) args))))))

(defn- create-element [tag-name & args]
  (if (has-props args)
    (if (empty? (rest args))
      (if (= tag-name "script")
        (list (list 'elements/script-element) (first args))
        (list (list 'elements/element-with-props tag-name) (first args)))
      (if (string? (second args))
        (list (list 'elements/element-with-props-and-string-child tag-name) (first args) (second args))
        (list (list 'elements/element-with-props-and-children tag-name) (first args) (apply list 'list (rest args)))))
    (if (empty? args)
      (list (list 'elements/element-empty tag-name))
      (if (string? (first args))
        (list (list 'elements/element-with-string-child tag-name) (first args))
        (list (list 'elements/element-with-children tag-name) (apply list 'list args) ))))) 

(defmacro html [& args] (apply create-element "html" args))
(defmacro head [& args] (apply create-element "head" args))
(defmacro title [& args] (apply create-element "title" args))
(defmacro script [& args] (apply create-element "script" args))
(defmacro title [& args] (apply create-element "title" args))
(defmacro body [& args] (apply create-element "body" args))
(defmacro div [& args] (apply create-element "div" args))
(defmacro ul [& args] (apply create-element "ul" args))
(defmacro li [& args] (apply create-element "li" args))
(defmacro pre [& args] (apply create-element "pre" args))
(defmacro h1 [& args] (apply create-element "h1" args))
(defmacro form [& args] (apply create-element "form" args))
(defmacro input [& args] (apply create-element "input" args))
(defmacro button [& args] (apply create-element "button" args))

;; Special function to allow adding variable strings as children
;; For Example, creating a component like this (div (:some-str-var object))
;; will throw an error due to the macro not knowing that will be a plain string
;; With this funcion instead, the string will be rendered as expected without any
;; parsing.
(defn plain-text [txt] (fn [_] txt))

;; (clojure.walk/macroexpand-all '((html (head))))

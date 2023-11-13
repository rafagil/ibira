(ns app.osmosi.ibira.elements)

(def void-elements #{"area"
                    "base"
                    "br"
                    "col"
                    "embed"
                    "hr"
                    "img"
                    "input"
                    "link"
                    "meta"
                    "param"
                    "source"
                    "track"
                    "wbr"})

(defn- parse-props [props]
  (clojure.string/trim
	 (reduce str (map #(str (name (first %)) "=\"" (second %) "\" ") props))))

(defn- has-props [lst]
  (and lst (= (type (first lst)) clojure.lang.PersistentArrayMap)))

(defn element-empty [tag-name]
  (str "<" tag-name "></" tag-name ">\n"))

(defn void-element-empty [tag-name]
  (str "<" tag-name ">\n"))

(defn element-with-props [tag-name props]
  (str "<" tag-name " " (parse-props props) "></" tag-name ">\n"))

(defn void-element-with-props [tag-name props]
  (str "<" tag-name " " (parse-props props) ">\n"))

(defn element-with-children [tag-name & children]
  (str "<" tag-name ">"
       (reduce str (flatten children))
       "</" tag-name ">\n"))

(defn element-with-string-child [tag-name child]
  (str "<" tag-name ">"
       child
       "</" tag-name ">\n"))

(defn element-with-props-and-children [tag-name props & children]
  (str "<" tag-name " " (parse-props props) ">\n"
       (reduce str (flatten children))
       "</" tag-name ">\n"))

(defn element-with-props-and-string-child [tag-name props child]
  (str "<" tag-name " " (parse-props props) ">"
       child
       "</" tag-name ">\n"))

(defn- create-element [tag-name & args]
  (if (has-props args)
    (if (empty? (rest args))
      (if (contains? void-elements tag-name)
        (list 'app.osmosi.ibira.elements/void-element-with-props tag-name (first args))
        (list 'app.osmosi.ibira.elements/element-with-props tag-name (first args)))
      (if (string? (second args))
        (list 'app.osmosi.ibira.elements/element-with-props-and-string-child tag-name (first args) (second args))
        (apply list 'app.osmosi.ibira.elements/element-with-props-and-children tag-name (first args) (rest args))))
    (if (empty? args)
      (if (contains? void-elements tag-name)
        (list 'app.osmosi.ibira.elements/void-element-empty tag-name)
        (list 'app.osmosi.ibira.elements/element-empty tag-name))
      (if (string? (first args))
        (list 'app.osmosi.ibira.elements/element-with-string-child tag-name (first args))
        (apply list 'app.osmosi.ibira.elements/element-with-children tag-name args))))) 

(defmacro html [& args] (apply create-element "html" args))
(defmacro head [& args] (apply create-element "head" args))
(defmacro title [& args] (apply create-element "title" args))
(defmacro script [& args] (apply create-element "script" args))
(defmacro title [& args] (apply create-element "title" args))
(defmacro link [& args] (apply create-element "link" args))
(defmacro body [& args] (apply create-element "body" args))
(defmacro div [& args] (apply create-element "div" args))
(defmacro span [& args] (apply create-element "span" args))
(defmacro ul [& args] (apply create-element "ul" args))
(defmacro li [& args] (apply create-element "li" args))
(defmacro pre [& args] (apply create-element "pre" args))
(defmacro h1 [& args] (apply create-element "h1" args))
(defmacro form [& args] (apply create-element "form" args))
(defmacro input [& args] (apply create-element "input" args))
(defmacro button [& args] (apply create-element "button" args))
(defmacro br [& args] (apply create-element "br" args))
(defmacro strong [& args] (apply create-element "strong" args))
(defmacro img [& args] (apply create-element "img" args))

;; (clojure.walk/macroexpand-all '(html (head) (title) (div (pre)) (div {:a "b"}) (div)))

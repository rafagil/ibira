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

(defmacro a [& args] (apply create-element "a" args))
(defmacro abbr [& args] (apply create-element "abbr" args))
(defmacro acronym [& args] (apply create-element "acronym" args))
(defmacro address [& args] (apply create-element "address" args))
(defmacro area [& args] (apply create-element "area" args))
(defmacro article [& args] (apply create-element "article" args))
(defmacro aside [& args] (apply create-element "aside" args))
(defmacro audio [& args] (apply create-element "audio" args))
(defmacro b [& args] (apply create-element "b" args))
(defmacro base [& args] (apply create-element "base" args))
(defmacro bdi [& args] (apply create-element "bdi" args))
(defmacro bdo [& args] (apply create-element "bdo" args))
(defmacro big [& args] (apply create-element "big" args))
(defmacro blockquote [& args] (apply create-element "blockquote" args))
(defmacro body [& args] (apply create-element "body" args))
(defmacro br [& args] (apply create-element "br" args))
(defmacro button [& args] (apply create-element "button" args))
(defmacro canvas [& args] (apply create-element "canvas" args))
(defmacro caption [& args] (apply create-element "caption" args))
(defmacro center [& args] (apply create-element "center" args))
(defmacro cite [& args] (apply create-element "cite" args))
(defmacro code [& args] (apply create-element "code" args))
(defmacro col [& args] (apply create-element "col" args))
(defmacro colgroup [& args] (apply create-element "colgroup" args))
(defmacro data [& args] (apply create-element "data" args))
(defmacro datalist [& args] (apply create-element "datalist" args))
(defmacro dd [& args] (apply create-element "dd" args))
(defmacro del [& args] (apply create-element "del" args))
(defmacro details [& args] (apply create-element "details" args))
(defmacro dfn [& args] (apply create-element "dfn" args))
(defmacro dialog [& args] (apply create-element "dialog" args))
(defmacro dir [& args] (apply create-element "dir" args))
(defmacro div [& args] (apply create-element "div" args))
(defmacro dl [& args] (apply create-element "dl" args))
(defmacro dt [& args] (apply create-element "dt" args))
(defmacro em [& args] (apply create-element "em" args))
(defmacro embed [& args] (apply create-element "embed" args))
(defmacro fieldset [& args] (apply create-element "fieldset" args))
(defmacro figcaption [& args] (apply create-element "figcaption" args))
(defmacro figure [& args] (apply create-element "figure" args))
(defmacro font [& args] (apply create-element "font" args))
(defmacro footer [& args] (apply create-element "footer" args))
(defmacro form [& args] (apply create-element "form" args))
(defmacro frame [& args] (apply create-element "frame" args))
(defmacro frameset [& args] (apply create-element "frameset" args))
(defmacro h1 [& args] (apply create-element "h1" args))
(defmacro head [& args] (apply create-element "head" args))
(defmacro header [& args] (apply create-element "header" args))
(defmacro hgroup [& args] (apply create-element "hgroup" args))
(defmacro hr [& args] (apply create-element "hr" args))
(defmacro html [& args] (apply create-element "html" args))
(defmacro i [& args] (apply create-element "i" args))
(defmacro iframe [& args] (apply create-element "iframe" args))
(defmacro image [& args] (apply create-element "image" args))
(defmacro img [& args] (apply create-element "img" args))
(defmacro input [& args] (apply create-element "input" args))
(defmacro ins [& args] (apply create-element "ins" args))
(defmacro kbd [& args] (apply create-element "kbd" args))
(defmacro label [& args] (apply create-element "label" args))
(defmacro legend [& args] (apply create-element "legend" args))
(defmacro li [& args] (apply create-element "li" args))
(defmacro link [& args] (apply create-element "link" args))
(defmacro main [& args] (apply create-element "main" args))
(defmacro html-map [& args] (apply create-element "map" args))
(defmacro mark [& args] (apply create-element "mark" args))
(defmacro marquee [& args] (apply create-element "marquee" args))
(defmacro menu [& args] (apply create-element "menu" args))
(defmacro menuitem [& args] (apply create-element "menuitem" args))
(defmacro html-meta [& args] (apply create-element "meta" args))
(defmacro meter [& args] (apply create-element "meter" args))
(defmacro nav [& args] (apply create-element "nav" args))
(defmacro nobr [& args] (apply create-element "nobr" args))
(defmacro noembed [& args] (apply create-element "noembed" args))
(defmacro noframes [& args] (apply create-element "noframes" args))
(defmacro noscript [& args] (apply create-element "noscript" args))
(defmacro object [& args] (apply create-element "object" args))
(defmacro ol [& args] (apply create-element "ol" args))
(defmacro optgroup [& args] (apply create-element "optgroup" args))
(defmacro option [& args] (apply create-element "option" args))
(defmacro output [& args] (apply create-element "output" args))
(defmacro p [& args] (apply create-element "p" args))
(defmacro param [& args] (apply create-element "param" args))
(defmacro picture [& args] (apply create-element "picture" args))
(defmacro plaintext [& args] (apply create-element "plaintext" args))
(defmacro portal [& args] (apply create-element "portal" args))
(defmacro xperimenta [& args] (apply create-element "xperimenta" args))
(defmacro pre [& args] (apply create-element "pre" args))
(defmacro progress [& args] (apply create-element "progress" args))
(defmacro q [& args] (apply create-element "q" args))
(defmacro rb [& args] (apply create-element "rb" args))
(defmacro rp [& args] (apply create-element "rp" args))
(defmacro rt [& args] (apply create-element "rt" args))
(defmacro rtc [& args] (apply create-element "rtc" args))
(defmacro ruby [& args] (apply create-element "ruby" args))
(defmacro s [& args] (apply create-element "s" args))
(defmacro samp [& args] (apply create-element "samp" args))
(defmacro script [& args] (apply create-element "script" args))
(defmacro search [& args] (apply create-element "search" args))
(defmacro section [& args] (apply create-element "section" args))
(defmacro select [& args] (apply create-element "select" args))
(defmacro slot [& args] (apply create-element "slot" args))
(defmacro small [& args] (apply create-element "small" args))
(defmacro source [& args] (apply create-element "source" args))
(defmacro span [& args] (apply create-element "span" args))
(defmacro strike [& args] (apply create-element "strike" args))
(defmacro strong [& args] (apply create-element "strong" args))
(defmacro style [& args] (apply create-element "style" args))
(defmacro sub [& args] (apply create-element "sub" args))
(defmacro summary [& args] (apply create-element "summary" args))
(defmacro sup [& args] (apply create-element "sup" args))
(defmacro table [& args] (apply create-element "table" args))
(defmacro tbody [& args] (apply create-element "tbody" args))
(defmacro td [& args] (apply create-element "td" args))
(defmacro template [& args] (apply create-element "template" args))
(defmacro textarea [& args] (apply create-element "textarea" args))
(defmacro tfoot [& args] (apply create-element "tfoot" args))
(defmacro th [& args] (apply create-element "th" args))
(defmacro thead [& args] (apply create-element "thead" args))
(defmacro html-time [& args] (apply create-element "time" args))
(defmacro title [& args] (apply create-element "title" args))
(defmacro tr [& args] (apply create-element "tr" args))
(defmacro track [& args] (apply create-element "track" args))
(defmacro tt [& args] (apply create-element "tt" args))
(defmacro u [& args] (apply create-element "u" args))
(defmacro ul [& args] (apply create-element "ul" args))
(defmacro var [& args] (apply create-element "var" args))
(defmacro video [& args] (apply create-element "video" args))
(defmacro wbr [& args] (apply create-element "wbr" args))
(defmacro xmp [& args] (apply create-element "xmp" args))

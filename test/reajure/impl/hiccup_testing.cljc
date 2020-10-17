(ns reajure.impl.hiccup-testing
  "Hiccup macro example setup. 
   Note: due to the way cljs macros work, these macro must be declared in their own file."
  (:require
   #?@(:cljs [[cljs.test :refer [is]]]
       :clj [[clojure.test :refer [is]]])
   #?(:clj [reajure.hiccup :as hiccup]))
  #?(:cljs (:require-macros [reajure.impl.hiccup-testing])))

#?(:cljs
   (defn
     el->type+props
     "Convert js element to its type+props tuple."
     [el]
     (let [type (.-type el)
           props (.-props el)]
       [type props])))

(defn is-el=
  "Ensure that compiled hiccup `el` matches env expectation.
   In cljs we match the js element type and props.
   In clj we match the outputed s-expression."
  #?@(:cljs ;; match js element to type+props
      ([el [type props]]
       (let [[el-type el-props] (el->type+props el)]
         (is (= el-type type))
         (is (= (js->clj el-props) (js->clj  props)))))
      :clj ;; match element fn call
      ([el sexp]
       (is (= el sexp)))))

#?(:clj
   (defmacro h
     ([body] `(h ~body {}))
     ([body opts] (hiccup/compile body opts))))

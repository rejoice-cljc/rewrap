(ns rewrap.test
  "Test helpers."
  (:require
   #?@(:cljs [[cljs.test :refer [is]]
              [goog.object :as obj]]
       :clj [[clojure.test :refer [is]]]))
  #?(:cljs (:require-macros [rewrap.test])))

;; object testing helpers 

(defn obj-eq
  "Checks whether two objects are equal to each other. 
   In clj, compares the s-expressions."
  [x1 x2]
  #?(:cljs (obj/equals x1 x2)
     :clj (= x1 x2)))

;; = element testing helpers 

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
         (is (= (js->clj el-props :keywordize-keys true) (js->clj  props :keywordize-keys true)))))
      :clj ;; match element fn call
      ([el sexp]
       (is (= el sexp)))))

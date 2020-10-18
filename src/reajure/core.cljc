(ns reajure.core
  (:require [reajure.impl.js-interop :as j]
            #?(:cljs ["react" :as react])))

(defn ->props
  "Convert cljs map to js props object
   In clj, returns [[cljs.core/obj]] s-expression with appropriate key-value pairs as arguments."
  ([m] (->props m (j/obj)))
  ([m o]
   (if (seq m)
     (recur (rest m)
            (let [[k v] (first m)]
              (j/onto-obj o (j/kw->str k) v)))
     (j/build-obj o))))

(defn render
  "Render react element."
  ([type] (render type nil))
  ([type props & children]
   #?(:cljs (apply react/createElement type props children)
      :clj `(react/createElement ~type ~props ~@children))))

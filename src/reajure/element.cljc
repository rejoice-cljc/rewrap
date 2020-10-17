(ns reajure.element
  #?(:cljs (:require ["react" :as react])))

(defn render
  ([type] (render type nil))
  ([type props & children]
   #?(:cljs (apply react/createElement type props children)
      :clj `(react/createElement ~type ~props ~@children))))

(ns reajure.render.element
  (:require ["react" :as react]))

(defn render
  ([type] (render type nil))
  ([type props & children]
   (println type props children)
   (apply react/createElement type props children)))

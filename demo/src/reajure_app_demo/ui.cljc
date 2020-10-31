(ns reajure-app-demo.ui
  (:require
   #?@(:clj [[reajure.core :as rj]
             [reajure.compile.hiccup :as hiccup]
             [reajure.compile.component :as comp]]))
  #?(:cljs (:require-macros [reajure-app-demo.ui])))

#?(:clj
   (do
     (defn hiccup
       [expr]
       (hiccup/compile
        expr
        {:parsers {keyword? {:tag #(-> % name str)}
                   any?     {:props rj/->props}}}))

     (defmacro h
       "Compile hiccup component."
       [expr]
       (hiccup expr))

     (defmacro defc
       "Define fn component."
       [& forms]
       (let [{:keys [name docstr component]} (comp/compile
                                              forms
                                              {:body (fn [xs] `[~@(butlast xs) ~(hiccup (last xs))])})]
         `(def ~@(if docstr [name docstr] [name])
            ~component)))))

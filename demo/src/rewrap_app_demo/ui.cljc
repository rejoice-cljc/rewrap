(ns rewrap-app-demo.ui
  (:require
   #?@(:clj [[rewrap.core :as rj]
             [rewrap.compile.hiccup :as hiccup]
             [rewrap.compile.component :as comp]]))
  #?(:cljs (:require-macros [rewrap-app-demo.ui])))

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

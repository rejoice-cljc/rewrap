(ns reajure-app.ui
  (:require
   #?@(:clj [[reajure.core :as rj]
             [reajure.hiccup :as hiccup]]))
  #?(:cljs (:require-macros [reajure-app.ui])))

#?(:clj
   (do
     (def parsers {keyword? {:tag #(-> % name str)}
                   any?     {:props rj/->props}})

     (defmacro h
       "Compile component hiccup."
       [body]
       (hiccup/compile body {:parsers parsers}))))

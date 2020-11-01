(ns rewrap-app-demo.ui
  (:require
   #?(:cljs ["react" :as react])
   #?@(:clj [[rewrap.hiccup :as hiccup]
             [rewrap.component :as comp]]))
  #?(:cljs (:require-macros [rewrap-app-demo.ui])))

#?(:cljs (def createElement react/createElement))

#?(:clj
   (do
     (defn emit-element "Emit react element expr."
       [type props & children]
       `(createElement ~type ~props ~@children))

     (defn hiccup "Compile component hiccup fn." 
       [expr]
       (hiccup/compile
        expr
        {:emitter emit-element
         :parsers {keyword? {:tag #(-> % name str)}
                   any?     {:props comp/->props}}}))

     (defmacro h "Compile component hiccup macro."
       [expr]
       (hiccup expr))

     (defmacro defc "Define fn component."
       [& forms]
       (let [{:keys [name docstr component]} (comp/compile
                                              forms
                                              {:body (fn [xs] `[~@(butlast xs) ~(hiccup (last xs))])})]
         `(def ~@(if docstr [name docstr] [name])
            ~component)))))

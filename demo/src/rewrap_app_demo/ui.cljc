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

     (defn compile-hiccup "Compile any hiccup in component expr."
       [component]
       (hiccup/compile component
                       {:emitter emit-element
                        :parsers [[keyword? {:tag #(-> % name str)}]
                                  [any?     {:props comp/->props}]]}))

     (defmacro h "Compile component hiccup macro."
       [component]
       (compile-hiccup component))

     (defmacro defc "Define fn component."
       [& decls]
       (let [{:keys [name docstr params body]} (comp/conform decls)
             eval-exprs       (butlast body)
             component-expr  (last body)
             element-expr (compile-hiccup component-expr)]
         `(def ~@(if docstr [name docstr] [name])
            ~(comp/generate [name docstr params eval-exprs element-expr]))))))

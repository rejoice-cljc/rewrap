(ns reajure.core
  (:refer-clojure :exclude [compile])
  (:require #?(:clj [reajure.compile.hiccup :as hiccup])
            #?(:cljs [reajure.render.element]))
  #?(:cljs (:require-macros [reajure.core])))

#?(:clj
   (defmacro default-emitter
     [tag props children]
     `(reajure.render.element/render ~tag ~props ~@children)))

#?(:clj
   (defmacro render
     ([body] `(render ~body {:emitter default-emitter}))
     ([body opts]
      (hiccup/compile body opts))))

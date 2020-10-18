(ns reajure.impl.js-interop
  (:refer-clojure :exclude [assoc])
  #?(:cljs (:require [goog.object :as obj])))

;; = keys interop 

(defn kw->str
  "Turns keyword to a string. 
   Note: also handles namespaced keywords."
  #?(:cljs [^cljs.lang.Keyword kw]
     :clj [kw])
  (str (symbol kw)))

;; = object interop 

(defn obj
  "Create new js object form."
  []
  #?(:clj '[cljs.core/js-obj]
     :cljs #js {}))

(defn onto-obj
  "Add key-value pair onto js object form.
   If clj, we're appending them as arguments; if cljs, we're setting them on the object itself."
  [o k v]
  #?(:clj (conj o k v)
     :cljs (doto o (obj/set k v))))

(defn build-obj 
  "Build final js object form.
   In clj, need to turn vector into list s-expression; in cljs, we return object itself."
  [o]
  #?(:cljs o
     :clj (list* o)))

(ns rewrap.dev.analyze
  "utilities for analyzing react component body.
   NOTE: This code was forked from lilactown/helix analyzer implementation."
  (:require [clojure.walk]))

(defn- find-forms
  "Recursively walks any component `form` and finds all sub-forms that match `pred`."
  [pred expr]
  (let [matches (atom [])]
    (clojure.walk/postwalk
     (fn [x]
       (when (pred x)
         (swap! matches conj x))
       x)
     expr)
    @matches))

(defn hook-sym?
  "Checks whether argument is a valid hook symbol.
   React convetion is for any hook to have 'use-' prefix."
  [x]
  (boolean
   (and (symbol? x)
        (some #(re-find % (name x))
              [#"^use\-"
               #"^use[A-Z]"]))))

(defn hook-expr?
  "Checks whether argument is a valid hook expr."
  [x]
  (and (list? x) (hook-sym? (first x))))

(defn find-hooks
  "Finds all hook exprs in component `body`."
  [body]
  (find-forms hook-expr? body))

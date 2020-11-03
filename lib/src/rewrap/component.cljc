(ns rewrap.component
  "Utilities for compiling React component definitions."
  (:refer-clojure :exclude [compile])
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])
   [rewrap.impl.js-interop :as j]))

;; -- Compiler utilites 

(defn ^:export ->props
  "Convert cljs map to js props object
   In clj, returns [[cljs.core/obj]] s-expression with appropriate key-value pairs as arguments."
  ([m] (->props m (j/obj)))
  ([m o]
   (if (seq m)
     (recur (rest m)
            (let [[k v] (first m)]
              (j/onto-obj o (j/kw->str k) v)))
     (j/build-obj o))))

(s/def ::def-decls
  (s/cat
   :name     symbol?
   :docstr   (s/? string?)
   :params   (s/coll-of any? :kind vector?)
   :body     (s/* any?)))

(defn conform
  "Conform component def declaration as per ::def-decls spec."
  [decls]
  (let [parsed (s/conform ::def-decls decls)]
    (if (= parsed :clojure.spec.alpha/invalid)
      (throw (ex-info "Invalid component definition." (s/explain-data ::def-decls parsed)))
      (let [return-expr (-> parsed :body last)
            eval-exprs (-> parsed :body butlast)]
        (cond-> (dissoc parsed :body)
          return-expr (assoc :return-expr return-expr)
          eval-exprs (assoc :extra-exprs eval-exprs))))))

(defn generate
  "Generate component fn expr with given `name`, `params`, and `body`.
   The fn body eval and return exprs can be passed separately with fn*/4."
  ([name params eval-exprs return-expr] (generate name params `(~@eval-exprs ~return-expr)))
  ([name params body]
   (let [n (count params)]
     `(let [fc-expr# (fn ~name
                       ~@(if (= n 0)
                           `([] ~@body)
                           (let [bindings (if (= n 1)
                                            '[props#]
                                            '[props# ref#])]
                             `(~bindings (let [~params ~bindings] ~@body)))))]
        ;; note: component name must be capitalied, right now prepending Reajure prefix to name
        ;; which will also help distinguish precompiled cljs components in devtools
        (set! (.-displayName fc-expr#) ~(str "Reajure__" name))
        fc-expr#))))

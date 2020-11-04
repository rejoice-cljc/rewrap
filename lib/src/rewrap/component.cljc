(ns rewrap.component
  "Utilities for compiling React component definitions."
  (:refer-clojure :exclude [compile])
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])
   [rewrap.impl.js-interop :as j]))

(defn ->props
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
  "Conform component def declarations."
  [decls]
  (let [parsed (s/conform ::def-decls decls)]
    (if (= parsed :clojure.spec.alpha/invalid)
      (throw (ex-info "Cannot conform component declaration." (s/explain-data ::def-decls parsed)))
      parsed)))

(defn- form 
  "Form component def decls from positional args.
   Allows passing body's eval-exprs and return-expr separately via form/5."
  ([name docstr params eval-exprs return-expr] (form name docstr params `[~@eval-exprs ~return-expr]))
  ([name docstr params body] (conform (remove nil? (list* name docstr params body)))))

(defn generate
  "Generate component fn from its def `decls` using custom `opts`.
   Decls can be conformed map or their positional arguments.
   Options:
   :wrap-props-param - fn, wrap bound props symbol to transform at runtime.
   :parse-display-name - fn, transform the name attached to react element."
  ([decls] (generate decls {}))
  ([decls {:keys [wrap-props-param parse-display-name]
           :or {wrap-props-param   identity
                parse-display-name identity}
           :as _opts}]
   (let [{:keys [name params body]} (cond (map? decls)    decls
                                          (vector? decls) (apply form decls)
                                          :else (ex-info "Invalid component declarations." {:decls decls}))
         num-args (count params)]
     `(let [fc-expr# (fn ~name
                       ~@(if (= num-args 0)
                           `([] ~@body)
                           (let [bindings (if (= num-args 1) '[props#] '[props# ref#])]
                             `(~bindings
                               (let [~params ~(update bindings 0 wrap-props-param)]
                                 ~@body)))))]
        ;; note: component name must be capitalied, right now prepending Reajure prefix to name
        ;; which will also help distinguish precompiled cljs components in devtools
        (set! (.-displayName fc-expr#) ~(str "Reajure__" (parse-display-name name)))
        fc-expr#))))

(ns reajure.compile.component
  "Utilities for compiling React component definitions."
  (:refer-clojure :exclude [compile])
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])
   [reajure.impl.parser :as parser]))

(s/def ::defnc-forms
  (s/cat
   :name    symbol?
   :docstr  (s/? string?)
   :params  (s/coll-of any? :kind vector?)
   :body    (s/* any?)))

(defn normalize-forms
  "Normalize component defn forms."
  [forms]
  (let [parsed (s/conform ::defnc-forms forms)]
    (if (= parsed :clojure.spec.alpha/invalid)
      (throw (ex-info "Invalid component definition." (s/explain-data ::defnc-forms parsed)))
      (let [{:keys [name docstr params body]} parsed]
        [name docstr params body]))))

(defn- apply-forms-map-parser
  "Apply parser map to normalized forms `nforms`.
   Accepts :name, :docstr, :params, :body parsing keys.
   A parser value can either be a transform fn (fn [x] x) or hardcoded value."
  [nforms {:keys [name docstr params body]
           :or {name   identity
                docstr identity
                params   identity
                body   identity}}]
  (letfn [(fn-or-val [f-or-v x] (if (fn? f-or-v) (f-or-v x) f-or-v))]
    (let [[x-name x-docstr x-params x-body] nforms]
      [(fn-or-val name x-name)
       (fn-or-val docstr x-docstr)
       (fn-or-val params x-params)
       (fn-or-val body x-body)])))

(defn fc-dname* 
  "Give a display `name` to a fn component expr."
  [fc-expr name]
  `(let [fc# ~fc-expr]
     (set! (.-displayName fc#) ~(str name))
     fc#))

(defn fc-expr*
  "Generate fn component expr with given `name`, `params`, and `body`."
  [name params body]
  (let [n (count params)]
    `(fn ~name
       ~@(if (= n 0)
           `([] ~@body)
           (let [bindings (if (= n 1)
                            '[props#]
                            '[props# ref#])]
             `(~bindings (let [~params ~bindings] ~@body)))))))

(defn compile
  "Compile component def `forms` using custom `parser`.
   Returns map with parsed :name, :docstr, :params, :body options, along their composed component form."
  ([forms] (compile forms {}))
  ([forms parser]
   (let [[name docstr params body] (parser/apply-parser (normalize-forms forms) parser apply-forms-map-parser)]
     {:name   name
      :docstr docstr
      :params params
      :body   body
      :component (-> (fc-expr* name params body)
                     (fc-dname* name))})))


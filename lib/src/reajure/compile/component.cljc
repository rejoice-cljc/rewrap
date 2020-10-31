(ns reajure.compile.component
  "Utilities for compiling React component definitions."
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])
   [reajure.impl.parser :as parser]))

(s/def ::defnc-forms
  (s/cat
   :name    symbol?
   :docstr  (s/? string?)
   :params    (s/coll-of any? :kind vector?)
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

(defn component-fn*
  "Generate component fn with given `name`, `params`, and `body`."
  [name params body]
  `(fn ~name
     ~@(case (count params)
         `([] ~@body)
         `([props#] (let [~params [props#]] ~@body))
         `([props# ?ref#] (let [~params [props# ?ref#]] ~@body)))))

(defn compile-def
  "Compile component `forms` def using custom `parser`.
   Returns map with parsed :name, :docstr, :params, :body options, along their composed component form."
  ([forms] (compile-def forms {}))
  ([forms parser]
   (let [[name docstr params body] (parser/apply-parser (normalize-forms forms) parser apply-forms-map-parser)]
     {:name name
      :docstr docstr
      :params params
      :body body
      :component (component-fn* name params body)})))


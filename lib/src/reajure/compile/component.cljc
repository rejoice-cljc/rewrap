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
   :args    (s/coll-of any? :kind vector?)
   :body    (s/* any?)))

(comment 
  (s/conform ::defnc-forms ['msg ['props] "text"]))

(defn normalize-forms
  "Normalize component defn forms."
  [forms]
  (let [parsed (s/conform ::defnc-forms forms)]
    (if (= parsed :clojure.spec.alpha/invalid)
      (throw (ex-info "Invalid component definition." (s/explain-data ::defnc-forms parsed)))
      (let [{:keys [name docstr args body]} parsed]
        [name docstr args body]))))

(defn- apply-forms-map-parser
  "Apply parser map to normalized forms `nforms`.
   Accepts :name, :docstr, :args, :body parsing keys.
   A parser value can either be a transform fn (fn [x] x) or hardcoded value."
  [nforms {:keys [name docstr args body]
           :or {name   identity
                docstr identity
                args   identity
                body   identity}}]
  (letfn [(fn-or-val [f-or-v x] (if (fn? f-or-v) (f-or-v x) f-or-v))]
    (let [[x-name x-docstr x-args x-body] nforms]
      [(fn-or-val name x-name)
       (fn-or-val docstr x-docstr)
       (fn-or-val args x-args)
       (fn-or-val body x-body)])))

(defn component-fn*
  "Generate component fn with given `name`, `args`, and `body`."
  [name args body]
  `(fn ~name
     ([] ~@body)
     ([props#] (~name props# nil))
     ([props# ?ref#] (let [~args [props# ?ref#]] ~@body))))

(defn compile-def
  "Compile component `forms` def using custom `parser`.
   Returns :name, :docstr, and :component map."
  ([forms] (compile-def forms {}))
  ([forms parser]
   (let [[name docstr args body] (parser/apply-parser (normalize-forms forms) parser apply-forms-map-parser)]
     {:name name
      :docstr docstr
      :component (component-fn* name args body)})))


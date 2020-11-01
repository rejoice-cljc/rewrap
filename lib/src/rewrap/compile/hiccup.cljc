(ns rewrap.compile.hiccup
  "Utilities for compiling Hiccup arguments into React components."
  (:refer-clojure :exclude [compile])
  (:require
   [rewrap.core]
   [rewrap.impl.sexp :as sexp]
   [rewrap.impl.parser :as parser]))

(declare compile)

(defn- js-literal?
  "Checks if type of `x` is a Javascript value."
  [x]
  #?(:cljs (or (object? x) (array? x))
     :clj  (instance? cljs.tagged_literals.JSValue x)))

(defn- maybe-hiccup?
  "Checks whether `x` could be a hiccup data."
  [x]
  (vector? x))

(defn- maybe-props?
  "Checks if argument `x` could be component props.
   Note: since we don't have access to JS object types, any JS literal is accepted as props."
  [x]
  (or (map? x) (js-literal? x) (nil? x)))

(defn- normalize-args
  "Converts component `args` to [tag props children] vector."
  [args]
  (let [tag      (first args)
        props    (if (maybe-props? (second args)) (second args) nil)
        children (into [] (drop (if (or props (nil? (second args))) 2 1) args))]
    [tag props children]))

(defn- apply-parser-args-map
  "Apply parser map to normalized component `args`.
   Accepts :tag, :props, and :children keys.
   A parser value can either be a transform fn (fn [x] x) or hardcoded value."
  [args {:keys [tag props children]
         :or {tag identity
              props identity
              children identity}}]
  (letfn [(fn-or-val [f-or-v x] (if (fn? f-or-v) (f-or-v x) f-or-v))]
    (let [[x-tag x-props x-children] args]
      [(fn-or-val tag x-tag)
       (if-not (js-literal? x-props) (when x-props (fn-or-val props x-props)) x-props)
       (fn-or-val children x-children)])))

(defn compile
  "Compile any hiccup in component `body` using custom `opts`.
   Options: 
      - :emitter      - fn or macro that accepts [tag props children] arguments.
      - :parsers      - {clause parser} map as specified in [[component/parse-args]].
      - :precompiled? - predicate fn, whether component tag and props need compilation.
      - :callable?    - predicate fn, whether component can be self-called after compilation."
  ([body] (compile body {}))
  ([body {:keys [emitter parsers precompiled? callable?]
          :or {emitter     rewrap.core/render
               parsers      {}
               precompiled? (fn [_] false)
               callable?    (fn [_] false)}
          :as opts}]
   (cond
     ;; => Hiccup
     (vector? body)
     (let [[initial-tag props children] (normalize-args body)]
       (if (precompiled? initial-tag)
         `(~initial-tag ~props ~@(mapv #(compile % opts) children))
         (let [parsers  (parser/create-parsers
                         parsers
                         {:children (fn [ch] (if (vector? ch) (mapv #(compile % opts) ch) ch))})
               args     (parser/parse
                         body
                         {:parsers          parsers
                          :normalize        normalize-args
                          :apply-parser-map apply-parser-args-map
                          :terminate-early? #(not (maybe-hiccup? %))})
               [t p ch]  args]
           (cond
              ;; => Callable tag
             (or (callable? initial-tag)
                  ;; note: Non-vector sequentials are expected to be self-callable code.
                 (and (not (vector? args)) (sequential? args)))
             `(~t ~p ~@ch)

             ;; => Emittable args 
             (vector? args)
             (if (fn? emitter)
               (apply emitter t p ch)
               `(~emitter ~t ~p ~@ch))

             :else (throw (ex-info "Invalid component argument." {:args args}))))))

     ;; => S-expression
     (list? body)
     (sexp/transform-output body #(compile % opts))

     :else body)))

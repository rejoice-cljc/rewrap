(ns reajure.hiccup
  (:refer-clojure :exclude [compile])
  (:require
   #?(:cljs [reajure.element])
   [reajure.impl.sexp :as sexp]
   [reajure.impl.parser :as parser])
  #?(:cljs (:require-macros [reajure.hiccup])))

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
  "Converts `args` to [tag props children] vector."
  [args]
  (let [tag      (first args)
        props    (if (maybe-props? (second args)) (second args) nil)
        children (into [] (drop (if (or props (nil? (second args))) 2 1) args))]
    [tag props children]))

(defn- apply-parser-args-opts
  "Apply parser component options map.
   Accepts :tag, :props, and :children parsing keys."
  [nargs {:keys [tag props children]
          :or {tag identity
               props identity
               children identity}}]
  (letfn [(fn-or-val [f-or-v x] (if (fn? f-or-v) (f-or-v x) f-or-v))]
    (let [[t p ch] nargs
          t (fn-or-val tag t)
          p (when p (if-not (js-literal? p) (fn-or-val props p) p))
          ch (fn-or-val children ch)]
      [t p ch])))

(defn- create-parsers
  "Merge custom `parsers` with default one for compiling hiccup children."
  [parsers opts]
  (merge parsers
        ;; We use custom inline fn as key so as to not override any user defined parsers.
         {#(identity %) {:children (fn [ch]
                                     (if (vector? ch) (mapv #(compile % opts) ch) ch))}}))

(defn default-emitter
  [tag props & children]
  #?(:cljs (apply reajure.element/render tag props children)
     :clj  `(reajure.element/render ~tag ~props ~@children)))

(defn compile
  "Compile any hiccup in component `body` using custom `opts`.
   Options: 
      - `emitter`      - fn or macro that accepts [tag props children] arguments.
      - `parsers`      - {clause parser} map as specified in [[component/parse-args]].
      - `precompiled?` - predicate fn, if component tag/props are already compiled, only hiccup children are compiled.
      - `callable?`    - preedicatte fn, component args are not precompiled but output itself can be self-called."
  ([body] (compile body {}))
  ([body {:keys [emitter parsers precompiled? callable?]
          :or {emitter      default-emitter
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
         (let [args (parser/parse body {:parsers         (create-parsers parsers opts)
                                        :normalize-args  normalize-args
                                        :apply-args-opts apply-parser-args-opts
                                        :terminate-early? #(not (maybe-hiccup? %))})
               [t p ch] args]
           (cond
              ;; => Callable tag
             (or (callable? initial-tag)
                  ;; note: Non-vector sequentials are expected to be self-callable code.
                 (and (not (vector? args)) (sequential? args)))
             `(~t ~p ~@ch)

             ;; => Emit args 
             (vector? args)
             (if (fn? emitter)
               (apply emitter t p ch)
               `(~emitter ~t ~p ~@ch))

             :else (throw (ex-info "Invalid component argument." {:args args}))))))

     ;; => S-expression
     (list? body)
     (sexp/transform-output body #(compile % opts))

     :else body)))

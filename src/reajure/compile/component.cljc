(ns reajure.compile.component
  "Compile component arguments.")

(defn js-literal? 
  "Checks if type of `x` is a Javascript value."
  [x]
  #?(:cljs (or (object? x) (array? x))
     :clj  (instance? cljs.tagged_literals.JSValue x)))

(defn props?
  "Checks if value `x` is component props.
   Note: since we don't have access to JS object types, any JS literal is accepted as props."
  [x]
  (or (map? x) (js-literal? x)))

(defn normalize-args
  "Converts `args` to [tag props children] vector."
  [args]
  (let [tag      (first args)
        props    (if (props? (second args)) (second args) nil)
        children (into [] (drop (if (or props (nil? (second args))) 2 1) args))]
    [tag props children]))

(defn- apply-map-parser
  "Apply parser map of :tag, :props, and :children options."
  [[t p ch] {:keys [tag props children]
             :or {tag identity
                  props identity
                  children identity}}]
  (letfn [(fval [fv x] (if (fn? fv) (fv x) fv))]
    [(fval tag t)
     (when p (if-not (js-literal? p) (fval props p) p))
     (fval children ch)]))

(defn- apply-parser
  "Apply `parser` on normalized args.
   Parser can be a function (fn [nargs] ,,,) or map with individual :tag, :props, :children options."
  [nargs parser]
  (if (fn? parser) (apply parser nargs) (apply-map-parser nargs parser)))

(defn- check-clause
  "Check whether given `clause` applies to `tag`. 
   Clause can be a predicate fn (e.g. symbol?) or primitive key (e.g. :<>) to match."
  [tag clause]
  (if (fn? clause) (clause tag) (= clause tag)))

(defn compile-args
  "Compile component `args` using `parsers`.
   Parsers are a {clause parser} as defined by [[check-clause]] and [[apply-parser]], respectively.
   If a parser returns anything other than a vector, parsing is terminated early."
  [args {:keys [parsers] :or {parsers {}}}]
  (let [nargs (normalize-args args)]
    (reduce (fn [acc [clause parser]]
              (let [tag (first acc)]
                (if (vector? acc)
                  (if (check-clause tag clause)
                    (apply-parser acc parser)
                    acc)
                  (reduced acc))))
            nargs
            parsers)))

(comment
  (def opts {:parsers (array-map
                          :<>      {:tag 'react/Fragment}
                          keyword? {:tag (fn [tag] `(ex/module* ~tag))}
                          any?     {:props    (fn [m]  `(ex/props* ~m))})})


  (compile-args [:<> 'child] opts)
  (compile-args [:vw {:style []}] opts)
  (compile-args [:vw {:style []} "Hello" "World"] opts))

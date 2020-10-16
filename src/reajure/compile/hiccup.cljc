(ns reajure.compile.hiccup
  (:refer-clojure :exclude [compile])
  (:require [reajure.compile.component :as comp]
            [reajure.compile.expression :as expr]))

(declare compile)

(defn create-parsers "Merge custom `parsers` with default one for compiling hiccup children."
  [parsers opts]
  (merge parsers
        ;; We use custom inline fn as key so as to not override any user defined parsers.
         {#(identity %) {:children (fn [ch] 
                                     (if (vector? ch) (mapv #(compile % opts) ch) ch))}}))

(defn compile
  "Compile any hiccup in component `body` using custom `opts`.
   Options: 
      - `emitter`      - create element macro that accepts [tag props children] arguments.
      - `parsers`      - {clause parser} map as specified in [[component/parse-args]].
      - `precompiled?` - predicate fn, if component tag/props are already compiled, only hiccup children are compiled.
      - `callable?`    - preedicatte fn, component args are not precompiled but output itself can be self-called."
  [body {:keys [emitter parsers precompiled? callable?]
         :or {parsers      {}
              precompiled? (fn [_] false)
              callable?    (fn [_] false)}
         :as opts}]
  (cond
    (vector? body) (let [[initial-tag props children] (comp/normalize-args body)]
                     (if (precompiled? initial-tag)
                        ;; Precompiled component children are spliced because they're spread inside macro body.
                       `(~initial-tag ~props ~@(mapv #(compile % opts) children))
                       (let [args (comp/compile-args body {:parsers (create-parsers parsers opts)})
                             [t p ch] args]
                         (cond
                           (or (callable? initial-tag)
                               ;; Non-vector sequentials are expected to be self-callable code.
                               (and (not (vector? args)) (sequential? args))) `(~t ~p ~@ch)
                           (vector? args) `(~emitter ~@args)
                           :else (throw (ex-info "Invalid component argument." {:args args}))))))
    (list? body) (expr/parse-output body #(compile % opts))
    :else body))

(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)

    (defmacro emitter [tag props children]
      `(.createElement react ~(name tag) (ex/->props ~props) ~@children))

    (defmacro h [body]
      (compile body {:emitter `emitter}))

    (defmacro component [props & children]
      `(emitter "custom" ~props ~@children)))

  ;; keyword vectors 
  (xpand '(h [:txt "Hello"]))
  (xpand '(h [:vw [:txt "Hello"]]))
  (xpand '(h [:vw [:txt "Hello"] [:txt "World"]]))
  (xpand '(h [:vw [:vw [:txt "hello"]]]))

  ;; fn lists
  (xpand '(h (let [msg "hello"] [:txt msg])))
  (xpand '(h [:vw (if bool [:txt "Yes"] [:txt "No"])]))
  (xpand '(h (let [msg "hello"] (when msg [:txt msg]))))

  ;; custom components
  (xpand `(h [component [:txt "hello"]]))
  (xpand `(h [component {:style []} [:txt "hello"]]))
  (xpand `(h [component (c/txt "hello")])))


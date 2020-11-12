# Rewrap

Standalone utilities for writing React in Clojurescript.

--- 

In the midst of opinionated Clojurescript-React wrappers, Rewrap isn't just another React wrapper. Rather, Rewrap provides utilities that can be used to create your own custom, opinionated React wrapper. In fact, we're using Rewrap in [Reajure](https://github.com/rejoice-cljc/reajure) to create a React Native UI kit in Clojurescript; the basic utilities required to write idiomatic React in Clojurescript, are provide here.

# Documentation

## Namespaces 

### `[rewrap.component :as comp]`

Utilities for compiling React components. 

To compile a component into a fn call you can use `comp/conform` and `comp/generate` utilities. The former parsers component declarations and the latter generates a fn call from them.

As an example, here's one way to create a custom component def macro, `defc`, for writing components:

```clj
(defmacro defc 
"Define fn component."
  [& decls]
  (let [{:keys [name docstr params body]} (comp/conform decls)
        eval-exprs      (butlast body)
        component-expr  (last body)
        element-expr    (compile-hiccup component-expr)]
    `(def ~@(if docstr [name docstr] [name])
      ~(comp/generate [name docstr params eval-exprs element-expr]))))))
```

Note, the `compile-hiccup` fn is not included, but, if desired, see next section for how to compile hiccup into react elements.

### [`rewrap.hiccup` :as hiccup]

Utilities for compiling Hiccup data.

---

To compile Hiccup data into React elements, you can use `rewrap.hiccup/compile`.

The following options are required: 
- `emitter`, a custom emitter fn or macro that converts arguments [type props & children] into desired expression.
- `parsers` a vector of hiccup data parsers, ran sequentially, each parser passing its output to the next one.

```clj
(defn emit-element 
"Emit react element expr."
  [type props & children]
  `(createElement ~type ~props ~@children))

(defn compile-hiccup 
"Compile any hiccup in a component body form."
  [form]
  (hiccup/compile form
                  {:emitter emit-element
                   :parsers [[keyword? {:tag #(-> % name str)}]
                             [any?     {:props clj->js}]]}))
```

### `rewrap.dev.refresh` 

Utilities for integrating with React Refresh runtime.

# Development

First, make sure dependencies are installed:

```
yarn install
```

Then, start the clojurescript development server: 

```
shadow-cljs server
```

In the shadow-cljs dashboard, you can run the builds configured in `shadow-cljs.edn`.


To run tests: 

- cljs: Run the shadow-cljs `test` build.
- clj: Run the `bb scripts/test` script.

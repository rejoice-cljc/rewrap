# Rewrap

In the midst of opinionated Clojurescript-React wrappers, Rewrap isn't just another React wrapper. Rather, Rewrap provides utilities that can be used to create your own custom React wrapper. That, in fact, is what we're using Rewrap for in another project - all the utilities we've found useful for writing React in idiomatic Clojurescript are extracted into this library.

## Usage

### Namespaces 

#### `rewrap.component`

`rewrap.component` holds utilities for generating React components. 

To convert a component into a fn call, you can use `component/conform` and `component/generate` utilities. The former parsers component declarations and the latter generates a fn call from them.

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

#### `rewrap.hiccup`

`rewrap.hiccup` holds utilities for compiling Hiccup data.

Primarily, you'll use `rewrap.hiccup/compile`, to convert Hiccup data into React element expressions.

The following options are required: 
- `emitter`, a custom emitter fn or macro that converts arguments [type props & children] into desired expression.
- `parsers` a vector of hiccup data parsers, ran sequentially, each parser passing its output to the next one.

As an example, here's how you'd setup a basic hiccup compiler: 

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
                   :parsers [ ;; convert any keywords into their respective string name, e.g. :div -> "div"
                             [keyword? {:tag #(-> % name str)}]
                              ;; on any component, just do a basic transform of its props from cljs to js
                             [any?     {:props clj->js}]]}))
```

#### `rewrap.dev.refresh` 

`rewrap.dev.refresh` holds utilities for integrating with React Refresh runtime.

In you're component macro, you need to embed the necessary logic required for React to refresh your component. You can generate these expressions with `refresh/exprs*`. 

```clj
{:keys [def-refresh init-refresh hookup-refresh]} (refresh/exprs* comp-id comp-sym comp-body)
```

Requires following positional arguments: 
- a unique id to associate to your component  
- a symbol that references your component object 
- the component body itself (it's walked so that any hooks can be found, a unique id of their contents can be generated)

Returns a map with the following keys: 
- `def-refresh`  - expression with required def declarations that must be present before component declaration.
- `init-refresh`  - expressions to embed after component declaration, so that react refresh can be initialized.
- `hookup-refresh` - expression to embed inside a component render, so that hook calls can be tracked.

## Development

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

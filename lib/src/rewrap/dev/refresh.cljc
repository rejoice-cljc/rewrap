(ns rewrap.dev.refresh
  "Utilities for integrating react-refresh live reloading during development."
  ;; react refresh integraton notes: https://github.com/facebook/react/issues/16604#issuecomment-528663101
  (:require
   #?@(:cljs [["react-refresh/runtime" :as refresh]
              [goog.object :as gobj]])
   #?@(:clj [[clojure.string :as str]
             [rewrap.dev.analyze :as ana]])))

;; todo enqueue updates, cleanup inject logic

#?(:cljs
   (do
     (defn setup!
       "Inject react-refresh runtime into window."
       []
       (refresh/injectIntoGlobalHook js/window)
       (gobj/set js/window "$RefreshReg$"  refresh/register)
       (gobj/set js/window "$RefreshSig$"  refresh/createSignatureFunctionForTransform))

     (defn perform!
       "Perform react refresh."
       []
       (refresh/performReactRefresh))

     (defn register!
       "Registers a component to react-refesh runtime."
       [comp id]
       (when-let [f (.-$RefreshReg$ js/window)]
         (f comp id)))

     (defn signature!
       "Create refresh signature for component rendering."
       []
       (when-let [f (.-$RefreshSig$ js/window)]
         (f)))))

#?(:clj
   (defn exprs*
     "Create react-refresh exprs to be included along component def in development.
      Returns map of: 
        :def-refresh    - expr with required defs used in other exprs. 
        :hookup-refresh - expr to embed in component render so hooks can be collected. 
        :init-refresh   - expr with fn calls needed to initialize refresh."
     [comp-id comp-sym comp-body]
     (let [sig-sym (gensym "refresh-sig")
           hooks-id (str/join (ana/find-hooks comp-body))
           ;; note: these are additional args to react-refresh signature call
           ;; settting placeholder values for now, they can be filled in if more fine-grained approach is needed
           force-reset?     false
           get-custom-hooks nil]
       {:def-refresh    `(if ^boolean goog/DEBUG
                           (def ~sig-sym (signature!)))
        :hookup-refresh `(if ^boolean goog/DEBUG
                                   ;; the second call to sig is for collecting component hook calls
                           (~sig-sym))
        :init-refresh   `(if ^boolean goog/DEBUG
                           (do
                            ;; creating initial component signature 
                             (~sig-sym ~comp-sym ~hooks-id ~force-reset? ~get-custom-hooks)
                            ;; register component in refresh runtime
                             (register! ~comp-sym ~comp-id)))})))

(ns rewrap-app-demo.ui-test
  (:require
   [cljs.test :refer [deftest testing]]
   [rewrap.test :as t]
   [rewrap-app-demo.ui :as ui]))

(ui/defc comp-basic
  []
  [:p "Foo"])

(ui/defc comp-with-props
  [^js props]
  [:p (.-text props)])

(deftest ui-h-compile-test
  (testing "compiles component hiccup"
    (t/is-el=
     (ui/h [:div {:foo "bar"}])
     ["div" #js {:foo "bar"}])))

(deftest ui-defc-compile-test
  (testing "compiles basic component"
    (t/is-el=
     (comp-basic)
     ["p" #js {:children "Foo"}]))

  (testing "compiles component with props"
    (t/is-el=
     (comp-with-props #js {:text "Foo"})
     ["p" #js {:children "Foo"}])))

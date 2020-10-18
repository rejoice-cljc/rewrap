(ns reajure-app.ui-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [reajure.test :as t]
   [reajure-app.ui :refer [h]]))

(deftest app-ui-test
  (testing "compiles component"
    (is (t/is-el=
         (h [:div {:foo "bar"}])
         ["div" #js {"foo" "bar"}]))))

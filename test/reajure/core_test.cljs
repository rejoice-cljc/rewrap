(ns reajure.core-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [reajure.core :as re]))

(deftest compile-hiccup
  (testing "foo"
    (let [el (re/render ["div" nil "hello"])]
      (is (= (.-type el) "div"))
      (is (= (-> el .-props .-children) "hello")))))

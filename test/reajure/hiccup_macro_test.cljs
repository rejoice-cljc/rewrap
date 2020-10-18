(ns reajure.hiccup-macro-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [reajure.testing :as t :refer [h]]))

(deftest hiccup-macro-test
  (testing ""
    (t/is-el=
     (h ["div"])
     ["div" #js {}])))

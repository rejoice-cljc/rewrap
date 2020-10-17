(ns reajure.hiccup-macro-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [reajure.impl.hiccup-testing :as ht :refer [h]]))

(deftest hiccup-macro-test
  (testing ""
    (ht/is-el=
     (h ["div"])
     ["div" #js {}])))

(ns reajure.compile.component-test
  (:require
   #?@(:cljs [[cljs.test :refer [deftest testing is]]]
       :clj [[clojure.test :refer [deftest testing is]]])
   [reajure.compile.component :as comp]))

(deftest comp-compile-test
  (testing "compiles basic forms"
    (is (comp/compile-def ['msg []])
        {:name 'msg :docstr nil :args []})
    (is (comp/compile-def ['msg "Docstring." []])
        {:name 'msg :docstr "Docstring." :args []})
    (is (comp/compile-def ['msg [] "Foo"])
        {:name 'msg :args [] :children ["Foo"]})))


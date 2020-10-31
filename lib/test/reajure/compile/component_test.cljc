(ns reajure.compile.component-test
  (:require
   #?(:cljs [cljs.test :refer [deftest testing is]]
      :clj  [clojure.test :refer [deftest testing is]])
   [clojure.string :as str]
   [reajure.compile.component :as comp]))

(deftest comp-compile-def-test
  (letfn [(testable [m] (dissoc m :component))]
   (testing "compiles basic forms"
     (is (= (testable
             (comp/compile-def ['msg []]))
            {:name 'msg :docstr nil :params [] :body nil}))
     (is (= (testable
             (comp/compile-def ['msg "Docstring." []]))
            {:name 'msg :docstr "Docstring." :params [] :body nil}))
     (is (= (testable
             (comp/compile-def ['msg [] "Foo"]))
            {:name 'msg :docstr nil :params [] :body ["Foo"]})))
   (testing "runs parser options"
     (is (= (testable
             (comp/compile-def
              ['msg [{:text "Foo"}]]
              {:name #(-> % str str/capitalize)
               :params #(update % 0 (fn [props] `(->clj ~props)))}))
            {:name "Msg" :docstr nil :params [`(->clj {:text "Foo"})] :body nil})))))


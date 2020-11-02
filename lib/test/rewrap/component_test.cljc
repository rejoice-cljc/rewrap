(ns rewrap.component-test
  (:require
   #?(:cljs [cljs.test :refer [deftest testing is]]
      :clj  [clojure.test :refer [deftest testing is]])
   [clojure.string :as str]
   [rewrap.test :as t]
   [rewrap.component :as comp]))

(deftest comp-props-test
  (testing "converts cljs map to js object"
    (is (t/obj= (comp/->props {:foo "bar"})
                  #?(:cljs #js {"foo" "bar"}
                     :clj `(cljs.core/js-obj "foo" "bar"))))))

(deftest comp-compile-def-test
  (letfn [(testable [m] (dissoc m :component))]
   (testing "compiles basic forms"
     (is (= (testable
             (comp/compile ['msg []]))
            {:name 'msg :docstr nil :params [] :body nil}))
     (is (= (testable
             (comp/compile ['msg "Docstring." []]))
            {:name 'msg :docstr "Docstring." :params [] :body nil}))
     (is (= (testable
             (comp/compile ['msg [] "Foo"]))
            {:name 'msg :docstr nil :params [] :body ["Foo"]})))
   (testing "runs parser options"
     (is (= (testable
             (comp/compile
              ['msg [{:text "Foo"}]]
              {:name #(-> % str str/capitalize)
               :params #(update % 0 (fn [props] `(->clj ~props)))}))
            {:name "Msg" :docstr nil :params [`(->clj {:text "Foo"})] :body nil})))))


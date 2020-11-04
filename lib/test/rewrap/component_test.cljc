(ns rewrap.component-test
  (:require
   #?(:cljs [cljs.test :refer [deftest testing is]]
      :clj  [clojure.test :refer [deftest testing is]])
   [rewrap.test :as t]
   [rewrap.component :as comp]))

(deftest component-conform
  (testing "conforms component forms"
    (is (= (comp/conform ['msg []])
           {:name 'msg :params []}))
    (is (=  (comp/conform ['msg "Docstring." []])
            {:name 'msg :docstr "Docstring." :params []}))
    (is (=  (comp/conform ['msg [] "Foo"])
            {:name 'msg :params [] :body ["Foo"]}))))

(deftest comp-props-test
  (testing "converts cljs map to js object"
    (is (t/obj= (comp/->props {:foo "bar"})
                  #?(:cljs #js {"foo" "bar"}
                     :clj `(cljs.core/js-obj "foo" "bar"))))))

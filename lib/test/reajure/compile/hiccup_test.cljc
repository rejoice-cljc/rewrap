(ns reajure.compile.hiccup-test
  (:require
   #?@(:cljs [[cljs.test :refer [deftest testing is]]]
       :clj [[clojure.test :refer [deftest testing is]]])
   [reajure.test :as t]
   [reajure.compile.hiccup :as hiccup]))

(deftest hiccup-compile-default-test
  (testing "accepts nil props"
    (t/is-el=
     (hiccup/compile ["div" nil])
     #?(:cljs ["div" #js {}]
        :clj `(reajure.core/render "div" nil))))

  (testing "accepts one child"
    (t/is-el=
     (hiccup/compile ["div" "foo"])
     #?(:cljs ["div" #js {:children "foo"}])
     #?(:clj `(reajure.core/render "div" nil "foo"))))

  (testing "accepts multiple children"
    (t/is-el=
     (hiccup/compile ["div" "foo" "bar"])
     #?(:cljs ["div" #js {:children #js ["foo" "bar"]}]
        :clj `(reajure.core/render "div" nil "foo" "bar"))))

  (testing "compiles nested children"
    (let [el (hiccup/compile ["div" ["p" "foo"]])
          #?@(:cljs [[type props] (t/el->type+props el)])]
      #?@(:cljs [(is (= type "div"))
                 (t/is-el=
                  (.-children props)
                  ["p" #js {:children "foo"}])]
          :clj [(is (= el `(reajure.core/render "div" nil (reajure.core/render "p" nil "foo"))))]))))


(deftest hiccup-compile-parser-test
  (testing "can parse an argument's hardcode value"
    (t/is-el=  (hiccup/compile [:div "foo"] {:parsers {keyword? {:tag "div"}}})
                #?(:cljs ["div" #js {:children "foo"}]
                   :clj `(reajure.core/render "div" nil "foo"))))

  (testing "can parse an argument's transform fn"
    (t/is-el=  (hiccup/compile [:div "foo"] {:parsers {keyword? {:tag name}}})
                #?(:cljs ["div" #js {:children "foo"}]
                   :clj `(reajure.core/render "div" nil "foo"))))

  (testing "terminates early if parser fn returns list"
    (is (= (hiccup/compile [:div "foo"] {:parsers {:div     (fn [_ _ ch] `(custom-el "div" ~ch))
                                                   keyword? {:tag :error}}})
           `(custom-el "div" "foo")))))

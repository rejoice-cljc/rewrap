(ns reajure.hiccup-test
  (:require
   #?@(:cljs [[cljs.test :refer [deftest testing is]]]
       :clj [[clojure.test :refer [deftest testing is]]])
   [reajure.impl.hiccup-testing :as ht]
   [reajure.hiccup :as hiccup]))

(deftest hiccup-default-compile-test
  (testing "accepts nil props"
    (ht/is-el=
     (hiccup/compile ["div" nil])
     #?(:cljs ["div" #js {}]
        :clj `(reajure.element/render "div" nil))))

  (testing "accepts one child"
    (ht/is-el=
     (hiccup/compile ["div" "foo"])
     #?(:cljs ["div" #js {:children "foo"}])
     #?(:clj `(reajure.element/render "div" nil "foo"))))

  (testing "accepts multiple children"
    (ht/is-el=
     (hiccup/compile ["div" "foo" "bar"])
     #?(:cljs ["div" #js {:children #js ["foo" "bar"]}]
        :clj `(reajure.element/render "div" nil "foo" "bar"))))

  (testing "compiles nested children"
    (let [el (hiccup/compile ["div" ["p" "foo"]])
          #?@(:cljs [[type props] (ht/el->type+props el)])]
      #?@(:cljs [(is (= type "div"))
                 (ht/is-el=
                  (.-children props)
                  ["p" #js {:children "foo"}])]
          :clj [(is (= el `(reajure.element/render "div" nil (reajure.element/render "p" nil "foo"))))]))))


(deftest hiccup-parser-compile-test
  (testing "can parse fn clause"
    (ht/is-el=  (hiccup/compile [:div "foo"] {:parsers {keyword? {:tag "div"}}})
          #?(:cljs ["div" #js {:children "foo"}]
             :clj `(reajure.element/render "div" nil "foo"))))

  (testing "terminates early if parser fn returns list"
    (is (= (hiccup/compile [:div "foo"] {:parsers {:div     (fn [_ _ ch] `(custom-el "div" ~ch))
                                                   keyword? {:tag :error}}})
           `(custom-el "div" "foo")))))

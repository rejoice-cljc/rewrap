(ns rewrap.tests.hiccup-test
  (:require
   #?(:cljs [cljs.test :refer [deftest testing is]]
      :clj  [clojure.test :refer [deftest testing is]])
   #?(:cljs ["react" :as react])
   [rewrap.test :as t]
   [rewrap.hiccup :as hiccup]))

(defn create-element
  [type props & children]
  #?(:cljs (apply react/createElement type props children)
     :clj `(react/createElement ~type ~props ~@children)))

(defn hiccup
  ([body] (hiccup body {}))
  ([body opts]
   (hiccup/compile body (merge {:emitter create-element} opts))))

(deftest hiccup-compile-default-test
  (testing "accepts nil props"
    (t/is-el=
     (hiccup ["div" nil])
     #?(:cljs ["div" #js {}]
        :clj `(create-element "div" nil))))

  (testing "accepts one child"
    (t/is-el=
     (hiccup ["div" "foo"])
     #?(:cljs ["div" #js {:children "foo"}])
     #?(:clj `(create-element "div" nil "foo"))))

  (testing "accepts multiple children"
    (t/is-el=
     (hiccup ["div" "foo" "bar"])
     #?(:cljs ["div" #js {:children #js ["foo" "bar"]}]
        :clj `(create-element "div" nil "foo" "bar"))))

  (testing "compiles nested children"
    (let [el (hiccup ["div" ["p" "foo"]])
          #?@(:cljs [[type props] (t/el->type+props el)])]
      #?@(:cljs [(is (= type "div"))
                 (t/is-el=
                  (.-children props)
                  ["p" #js {:children "foo"}])]
          :clj [(is (= el `(create-element "div" nil (create-element "p" nil "foo"))))]))))

(deftest hiccup-compile-parser-test
  (testing "can parse hardcode value"
    (t/is-el=  (hiccup [:div "foo"] {:parsers [[keyword? {:tag "div"}]]})
                #?(:cljs ["div" #js {:children "foo"}]
                   :clj `(create-element "div" nil "foo"))))

  (testing "can parse transform fn"
    (t/is-el=  (hiccup [:div "foo"] {:parsers [[keyword? {:tag name}]]})
                #?(:cljs ["div" #js {:children "foo"}]
                   :clj `(create-element "div" nil "foo"))))

  (testing "terminates early if parser fn returns list"
    (is (= (hiccup [:div "foo"] {:parsers [[:div     (fn [_ _ ch] `(custom-el "div" ~ch))]
                                           [keyword? {:tag :error}]]})
           `(custom-el "div" "foo")))))

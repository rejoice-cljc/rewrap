(ns reajure.compile.component-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [reajure.compile.component :as comp]))

(deftest compile-args
  (testing "can use multiple parsers"
    (is (= (comp/compile-args [:txt "Foo"]
                              {:parsers {:txt     {:tag "text"}
                                         keyword? {:tag #(name %)}
                                         any?     {:tag (fn [t] (str/capitalize t))}}})
           ["Text" nil ["Foo"]])))

  (testing "terminates parsing if list is found"
    (is (= (comp/compile-args [:txt "Foo"]
                         {:parsers {:txt     (fn [_ _ ch] `(el "text" ~ch))
                                    keyword? {:tag :error}}})
           `(el "text" ["Foo"])))))

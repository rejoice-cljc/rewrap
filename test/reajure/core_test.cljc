(ns reajure.core-test
  (:require
   #?@(:cljs [[cljs.test :refer [deftest testing is]]
              [goog.object :as obj]]
       :clj [[clojure.test :refer [deftest testing is]]])

   [reajure.core :refer [->props]]))

(defn eq [x1 x2]
  #?(:cljs (obj/equals x1 x2)
     :clj (= x1 x2)))

(deftest props-test
  (testing "converts cljs map to js object"
    (is (eq (->props {:foo "bar"})
            #?(:cljs #js {"foo" "bar"}
               :clj `(cljs.core/js-obj "foo" "bar"))))))


(ns rewrap.core-test
  (:require
   #?@(:cljs [[cljs.test :refer [deftest testing is]]]
       :clj [[clojure.test :refer [deftest testing is]]])
   [rewrap.test :as t]
   [rewrap.core :refer [->props]]))

(deftest props-test
  (testing "converts cljs map to js object"
    (is (t/obj-eq (->props {:foo "bar"})
            #?(:cljs #js {"foo" "bar"}
               :clj `(cljs.core/js-obj "foo" "bar"))))))


(ns pressure-it.t-coll
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            ))

(deftest conj-t
  (is (= [1 2 3] (conj [1 2] 3)))
  (is (= [3 1 2] (conj (list 1 2) 3)))
  (is (= [1 2 [3 4]] (conj [1 2] [3 4])))
  (is (= [1 2 [3 4]] (conj [1 2] '(3 4)))))


(deftest flatten-t
  (is (= [1 2 3 4] (concat [1 2] [3 4] ())))
  (is (= [1 2 3 4] (apply concat '([1 2] [3 4] ())))))


(fact "assoc in"
      (assoc-in {:a 1 :b {:c 1}} [:b :c] 100) => {:a 1 :b {:c 100}}
      (assoc-in {:a 1 :b [1]} [:b 1] 100) => {:a 1 :b [1 100]})

(fact "ref alter"
      (let [r (ref [])]
        (dosync (alter r conj 5))
        @r => [5]
        )
      )

(fact "sort fn"
      (sort [1 5 2 9]) => [1 2 5 9]
      (sort > [1 5 2 9]) => [9 5 2 1]
      (sort-by last {:a 5 :b 10 :c 7}) => '([:a 5] [:c 7] [:b 10])
      )

(fact "iterate over map"
      (doseq [[k v] {:a 1 :b 2}]
        (type k) => clojure.lang.Keyword
        (type v) => java.lang.Long)
      (count {:a 1 :b 2}) => 2
      )

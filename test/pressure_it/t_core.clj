(ns pressure-it.t-core
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clojure.string :as cstr]
            [pressure-it.core :as core]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]))


(against-background
 [(around :facts (let [prepared (core/prepare-users (atom {}) [[nil ["http://www.baidu.com"] 10 10]])] ?form))]
 (facts "userdefs test"
       (fact "should return 10 users"
             (count prepared) => 10)
        (fact "each item is like [nil baidu-user-request 10 10 #<BasicCookieStore []>]"
              (filter #(not= 5 (count %)) prepared) => ())
        ))

(against-background
 [(around :facts (let [ag (agent 0)] ?form))]
 (facts
  (fact "when aget call value function"
        (let [n (System/currentTimeMillis)]
          (send ag + (future (Thread/sleep 1000) 100))
          (< (- (System/currentTimeMillis) n) 30) => true)
   )))

(against-background
 [(around :facts (let [x 0] ?form))]
 (facts
  (fact "lazy map will cause future to exec?"
        (let [n (System/currentTimeMillis)
              futures (map #(future (Thread/sleep 10) %) (range 5))]
          (doseq [_ futures] nil)
          (Thread/sleep 1000)
          (count (filter (complement realized?) futures)) => 0
          ))))

(fact "replace string"
      (cstr/replace "abc{{rand}}cd{{rand}}" #"\{\{rand\}\}" "1") => "abc1cd1"
      (cstr/replace "abc{{rand}}cd{{rand}}" "{{rand}}" "1") => "abc1cd1"
      (cstr/replace "abc{{rand}}cd{{rand}}" "{{rand1}}" "1") => "abc{{rand}}cd{{rand}}"
      (count (core/random-str 8)) => 8
      (count (cstr/replace "abc{{rand}}cd{{rand}}" "{{rand}}" (core/random-str 8))) => 21)

(fact "test baidu home page"
      (let [urls ["http://www.baidu.com"]
            result-atom (core/benchmark [nil urls 10 10])]
        (count @result-atom) => 2
        (count (:thread-ids @result-atom)) => 10
        (let [ress (vals (dissoc @result-atom :thread-ids)) ]
          (doseq [r ress]
            (count r) => 100 ))))

(fact "unreachable urls, {:len 0, :request-time 0, :status -1}"
      (let [unurl "http://172.19.223.223"
            unr {:len 0, :request-time 0, :status -1}
            urls ["http://www.baidu.com" unurl]
            result-atom (core/benchmark [nil urls 2 1])]
        (count @result-atom) => 3
        (count (:thread-ids @result-atom)) => 2
        (@result-atom unurl) => (repeat 2 unr)))


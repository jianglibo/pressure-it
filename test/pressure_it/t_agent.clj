(ns pressure-it.t-agent
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clojure.tools.logging :as log]
            [clj-http.client :as client])
  (:import (java.util.concurrent Executors)))

(fact "agent order"
      (let [a (agent [])]
        (doseq [x (range 10)]
          (send a conj x))
        (await a)
        @a => (range 10)))

(defn thread-num
  [myatom av]
  (Thread/sleep 100)
  (swap! myatom conj (Thread/currentThread))
  0)

(fact "agent thread-num"
      (let [ato (atom #{})
            tnf (partial thread-num ato)
            ags (repeatedly 20 #(agent 0))
            sta (System/currentTimeMillis)]
        (doseq [a ags]
          (send a tnf)
          )
        (< (- (System/currentTimeMillis) sta) 100) => true
;        (apply await ags)
        (< (count @ato) 2) => true
        (apply await ags)
        (> (count @ato) 2) => true))

(fact "agent thread-num send-off"
      (let [tnums 20
            ato (atom #{})
            custom-pool (Executors/newFixedThreadPool tnums)
            tnf (partial thread-num ato)
            ags (repeatedly tnums #(agent 0))
            sta (System/currentTimeMillis)]
        (doseq [a ags]
          (send-via custom-pool a tnf))
        (< (- (System/currentTimeMillis) sta) 100) => true
;        (apply await ags)
        (< (count @ato) 2) => true
        (apply await ags)
        (count @ato) => 20))

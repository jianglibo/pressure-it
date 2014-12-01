(ns pressure-it.core
  (:require [clojure.tools.logging :as log]
            [clojure.string :as cstr]
            [clojure.pprint :as pprint]
            [clj-http.client :as client])
  (:import (java.util.concurrent Executors)))


(defn random-str [size]
  (cstr/join (take size (repeatedly #(rand-nth "0123456789abcdefghijklmnopqrstuvwxyz")))))

(defn save-response
  [result-atom newres url]
  (let [len (if-let [hlen (-> newres :headers (get "Content-Length"))]
              (Long/valueOf hlen)
              (count (:body newres)))
        status (:status newres)
        request-time (:request-time newres)]
;    (log/info "len:" len) (log/info "status type:" (type status)) (log/info "request-time type:" (type request-time))
    (swap! result-atom (fn [av r]
                         (let [len (count (av url))]
                           (assoc-in av [url len] r)))
           (-> {}
               (assoc :status status)
               (assoc :request-time request-time)
               (assoc :len len)))))

(defn- fetch-urls
  "fetch list of urls"
  [result-atom cs urls]
  (let [nurls (map #(cstr/replace % "{{rand}}" (random-str 8)) urls)]
    (doall
     (map (fn [nurl url]
            (save-response result-atom (try (client/get nurl {:cookie-store cs :socket-timeout 3000 :conn-timeout 3000})
                                         (catch Exception e (condp re-find (.getMessage e)
                                                              #"time out" {:status -100 :request-time 0}
                                                              {:status -1 :request-time 0}
                                                              ))) url))
            nurls urls))))


(defn- report-one-url
  [url reslist]
  (let [request-num (count reslist)
        fail-list (filter #(not= (:status %) 200) reslist)
        success-list (filter #(= (:status %) 200) reslist)
        total-download (reduce (fn [m v] (+ m (:len v))) 0 reslist)
        sucess-sorted (sort-by :request-time success-list)
        slowest (:request-time (last sucess-sorted))
        fastest (:request-time (first sucess-sorted))
        total-req-time (reduce #(+ %1 (:request-time %2)) 0 sucess-sorted)
        average (if (> total-req-time 0)
                  (float (/ total-req-time (count sucess-sorted)))
                  -1)]
    {url {:request-num request-num
          :total-download total-download
          :success {:request-num (count success-list)
                    :slowest slowest
                    :fastest fastest
                    :average average}
          :failure {:request-num (count fail-list)}}}))

(defn report-result
  [result-atom]
  (let [result @result-atom
        threads (:thread-ids result)
        result-map (dissoc result :thread-ids)]
    (log/info "thread number: " (count threads))
    (with-out-str (pprint/pprint (map #(report-one-url (key %) (val %)) result-map)))))

(defn do-login
  [userdefs]
  (doseq [ud userdefs]
    (if-let [lgf (first ud)]
      (lgf (last ud)))))

(defn prepare-users
  [result-atom userdefs]
  (doall (map (fn [ud] (let [urls (ud 1)]
                         (doall (map #(swap! result-atom assoc % []) urls))
                        )) userdefs))
  (let [nuserdefs (map (fn [ud]
                         (conj ud (clj-http.cookies/cookie-store)))
                       userdefs)
        users (apply concat (map #(repeat (% 2) %) nuserdefs))]
    (swap! result-atom assoc :thread-ids #{})
    (do-login nuserdefs)
    users))

(defn do-requests
  "this is a template, you can define other users too.it return a list of response list"
  [result-atom cs urls repeat-times]
  (client/with-connection-pool {:timeout 5 :threads 1 :insecure? false :default-per-route 10}
    (swap! result-atom (fn [av tid]
                         (let [tids (:thread-ids av)]
                           (assoc av :thread-ids (conj tids tid))))
                         (-> (Thread/currentThread) .getId))
    (dotimes [_ repeat-times]
      (fetch-urls result-atom cs urls))))

(defn benchmark
  "How many user repeat how many times.
  userdefs: [login-fn urls num rpt] [login-fn urls num rpt]
  user: [nil urls 10 10 #<BasicCookieStore []>]"
  [& userdefs]
  (let [result-atom (atom {})
        users (prepare-users result-atom userdefs)
        starttime (System/currentTimeMillis)
        futures (map #(future-call (partial do-requests result-atom (last %) (second %) (% 3))) users)]
    (doall futures)
    (doall (map deref futures))
    (log/info (report-result result-atom))
    (log/info "total time costs: " (- (System/currentTimeMillis) starttime))
    result-atom))


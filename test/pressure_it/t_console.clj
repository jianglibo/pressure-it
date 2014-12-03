(ns pressure-it.t-console
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clj-http.client :as client]
            [pressure-it.core :as core]
            [midje.sweet :refer :all]))

(fact "save url content as file"
      (let [res (client/get "http://www.nblr.gov.cn/RandomImage" {:as :byte-array})
            ct (get-in res [:headers "Content-Type"])
            fnname "log/captcha.jpg"]
        (with-open [w (io/output-stream fnname)]
          (.write w (:body res)))
        (> (.length (io/file fnname)) 300) => truthy))

(fact "read from console"
      (println "please enter \"abc\"")
      (let [rl (read-line)]
        rl => "abc"))

(fact "open mspaint.exe"
      (let [excode (core/get-captcha (clj-http.cookies/cookie-store) "http://www.nblr.gov.cn/RandomImage" ".jpg")]
        (:exit excode) => 0))

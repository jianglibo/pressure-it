(ns pressure-it.t-console
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [midje.sweet :refer :all]))

(fact "save url content as file"
      (let [res (client/get "http://www.nblr.gov.cn/RandomImage" {:as :byte-array})
            ct (get-in res [:headers "Content-Type"])
            fn "log/captcha.jpg"]
        (with-open [w (io/output-stream fn)]
          (.write w (:body res)))
        (> (.length (io/file fn)) 300) => truthy))

(fact "read from console"
      (println "please enter \"abc\"")
      (let [rl (read-line)]
        rl => "abc"))

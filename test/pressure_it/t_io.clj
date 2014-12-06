(ns pressure-it.t-io
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]))

(fact "read baidu home page to string"
      (let [sr (java.io.StringWriter.)]
        (io/copy
         (io/input-stream (io/as-url "http://www.baidu.com")) sr)
         (.toString sr) => #"百度一下"))


(fact "correct chao string."
      (let [sr (java.io.StringWriter.)]
        (io/copy
         (io/input-stream (io/as-url "http://www.baidu.com")) sr :encoding "GBK")
        (String. (char-array (take 1000 (-> sr
            .toString
            (.getBytes "GBK")
            (String. "UTF-8")))))  => #"百度一下"))

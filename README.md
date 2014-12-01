# pressure-it

A Clojure library designed to pressure web apps.

## Usage

checkout this project. then create a clj file like below.

```
(defn oa-user-login
  [cs]
  (let [login-url "http://someappurl.cc/Login"]
    (client/post login-url {:form-params {:fLoginVerification 1
                                          :Username "username"
                                          :Password "password"
                                          :DBPath "/domcfg.nsf"
                                          :Path_Info "/index.nsf"
                                          :Path_Info_Decoded "/index.nsf"
                                          :SaveOptions 1
                                          :$PublicAccess 1}
                            :cookie-store cs})))

(def oa-user-request-urls
  ["http://someurl.cc"
   "http://someurl.cc/RndStr={{rand}}"])

(pressure-it/benchmark [oa-user-login oa-user-request-urls 20 5])

```
run it. will see report in log/app.log like this.

```
5917095 [nREPL-worker-17] INFO  pressure-it.core  - ({"http://someurl/db_publicaffair.nsf/Toppic?OpenForm&RndStr={{rand}}"
  {:request-num 100,
   :total-download 526300,
   :success
   {:request-num 100, :slowest 2792, :fastest 326, :average 1548.93},
   :failure {:request-num 0}}}
 {"http://someurl/?OpenForm&path=/db_mqhb.nsf&fTitle=%E6%B0%91%E6%83%85%E4%BC%9A%E5%8A%9E&RndStr={{rand}}"
  {:request-num 100,
   :total-download 1288500,
   :success
   {:request-num 100, :slowest 2953, :fastest 2780, :average 2875.05},
   :failure {:request-num 0}}}
 {"http://someurl/db_printview.nsf/PeoplePrintView?OpenAgent&infoid=mqhb_Info&path=/4app&dbname=db_mqhb.nsf"
  {:request-num 100,
   :total-download 1164600,
   :success
   {:request-num 100, :slowest 2857, :fastest 153, :average 1605.19},
   :failure {:request-num 0}}})

```

## License

Copyright © 2014 jianglibo@gmail.com

Distributed under the Eclipse Public License either version 1.0.

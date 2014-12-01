(ns pressure-it.pressure-tpl
  (:require [clojure.tools.logging :as log]
            [clojure.string :as cstr]
            [clojure.pprint :as pprint]
            [pressure-it.core :as pressure-it]
            [clj-http.client :as client]))

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


(comment
 (pressure-it/benchmark [login-method urllist-tovisit howmany-users repeat-times])

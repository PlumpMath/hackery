(ns hackery.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clojure.core.async :as async
             :refer [<! >! <!! timeout chan alt! go
                     go-loop alts! close! put!]]))

(def ^:const token (slurp "token"))
(def ^:const host "https://api.github.com")

(defn req
  ([url]
     @(http/get
       (str host url)
       {:headers {"Authorization" (format "token %s" token)
                  "Accept" "application/vnd.github.v3+json"}}))
  ([url chan]
     (http/get
      (str host url)
      {:headers {"Authorization" (format "token %s" token)
                 "Accept" "application/vnd.github.v3+json"}}
      #(put! chan %))))

(defn read-orgs
  [{:keys [body] :as resp}]
  (json/parse-string body true))

(defn mapcatting [f]
  (fn [f1]
    (fn [result input]
      (reduce f1 result (f input)))))

(def orgs-chan (chan 1 (mapcatting read-orgs)))

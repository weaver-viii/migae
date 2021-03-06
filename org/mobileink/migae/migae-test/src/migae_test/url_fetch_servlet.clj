(ns migae-test.url-fetch-servlet
    (:gen-class :extends javax.servlet.http.HttpServlet)
    (:require [migae-test.url-fetch-impl :as impl]
            [ring.util.servlet :as ring]))

(defn -service
  [this rqst resp]
    (let [request-map  (ring/build-request-map rqst)
          response-map (impl/url-fetch-handler request-map)]
    (when response-map
      (ring/update-servlet-response resp response-map))))

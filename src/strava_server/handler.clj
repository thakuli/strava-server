(ns strava-server.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [strava-server.utils :as utils]
            [oauth2-client.core :as oauth2]
            [oauth2-client.ring :as oauth2-ring]            
            [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.codec :refer [base64-encode]]
            [oauth2-client.core :as oauth2]
            [oauth2-client.ring :as oauth2-ring]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))


(def oauth2-config
  {:authorization-uri "https://www.strava.com/oauth/authorize"
   :access-token-uri "https://www.strava.com/oauth/token"
   :client-id ""
   :client-secret ""
   :redirect-uri "http://127.0.0.1:3000/strava-callback"
   :scope nil})

(def per-page (or (System/getenv "STRAVA_PER_PAGE") 
                  "50"))

(def strava-api-url "https://www.strava.com/api/v3")
(def activities-url (str strava-api-url "/athlete/activities?per_page=" per-page))

(defn get-polylines [request]
  (let [ activities (json/read-json (oauth2-ring/do-authorized
                                     oauth2-config request
                                     #(oauth2/authorized-request
                                       :get % activities-url  (oauth2/auth-headers "access_token" %))
                                     :body)) ]
       (-> (utils/get-activity-data  activities)
           (utils/activity-data->json))))

(defn my-oauth2-wrap [url request method]
  (oauth2-ring/do-authorized
   oauth2-config request
   #(oauth2/authorized-request
     (keyword method) % url  (oauth2/auth-headers "access_token" %))
   :body))

(defn get-user-info [request]
  (my-oauth2-wrap "https://www.strava.com/api/v3/athlete" request "get"))


(defn get-polylines-2 []
  (let [ activities (utils/get-data utils/activities-url utils/options)]
    (-> (utils/get-activity-data  activities)
               (utils/activity-data->json))))

(defn get-page [page]
  (slurp (str (io/resource page))))

(defroutes app-routes
  (GET "/" [] (get-page "index.html"))
  (GET "/secret" [] (get-page "secret.html"))
  (GET "/map" [] (get-page "map.html"))
  (GET "/logout" request (assoc (response/redirect "/") :session {}))
  (GET "/activities" [] (fn [request] (get-polylines request)))  
  (GET "/strava-user-info" request (get-user-info request))

  (GET "/strava-user-info-2" request 
       (oauth2-ring/do-authorized
        oauth2-config request
        #(oauth2/authorized-request
          :get % "https://www.strava.com/api/v3/athlete" (oauth2/auth-headers "token" %))
        :body))
  (GET "/strava-callback" request
       (oauth2-ring/oauth2-callback-handler oauth2-config request))
  (GET "/activities" [] (get-polylines))
  (route/not-found "Not Found oho"))

(def app
  (-> #'app-routes
      (wrap-session)
      (wrap-params)))

;;(def app
;;  (wrap-defaults app-routes site-defaults))


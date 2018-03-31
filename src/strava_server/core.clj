(ns strava-server.core
  (:require  [compojure.core :refer [GET defroutes]]
             [compojure.route :as route]             
             [oauth2-client.core :as oauth2]
             [oauth2-client.ring :as oauth2-ring]
             [ring.adapter.jetty :as ring-jetty]
             [ring.util.codec :refer [base64-encode]]
             [ring.middleware.params :refer [wrap-params]]
             [ring.middleware.session :refer [wrap-session]]
             [ring.util.response :as response]
             [strava-server.utils :as utils]
             [strava-server.testi :as testi]             
             [clojure.data.json :as json]))

;; [oauth2-client.examples-utils :refer [pprint-response-body]]

-
(def oauth2-config
  {:authorization-uri "https://www.strava.com/oauth/authorize"
   :access-token-uri "https://www.strava.com/oauth/token"
   :client-id "21184"
   :client-secret "043d5615756549f7301408ac542d7cc5f351d2e6"
   :redirect-uri "http://127.0.0.1:3000/strava-callback"
   :scope nil})

(def strava-api-url "https://www.strava.com/api/v3")
(def activities-url (str strava-api-url "/athlete/activities?per_page=200"))
(def logout-url "https://www.strava.com/oauth/deauthorize")
(def athlete-clubs "https://www.strava.com/api/v3/athlete/clubs")

;; (defn run-jetty
;;   []
;;   (ring-jetty/run-jetty app-handler {:join? false :port 3000}))

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (ring-jetty/run-jetty app-handler {:join? false :port 3000}))

(defn get-page-2 [page]
  (slurp (str "resources/" page)))

(defn get-page [page]
  (println (str "requesting page " page))
  (slurp (str "/" page)))

(defn get-polylines [request]
  (let [ activities (json/read-json (oauth2-ring/do-authorized
                                     oauth2-config request
                                     #(oauth2/authorized-request
                                       :get % activities-url  (oauth2/auth-headers "access_token" %))
                                     :body)) ]
    (println (str "get-polylines: " request))
    (-> (utils/get-activity-data  activities)
        (utils/activity-data->json))))

(defn logout [request]
  (let [body (oauth2-ring/do-authorized
              oauth2-config request
              #(oauth2/authorized-request
                :post % logout-url (oauth2/auth-headers "access_token" %))
              :body)]
    (assoc (response/redirect "/") :session {})
    body))

(defn my-oauth2-wrap [url request method]
  (println (str "my-oauth2-wrap: " request))
  (oauth2-ring/do-authorized
   oauth2-config request
   #(oauth2/authorized-request
     (keyword method) % url  (oauth2/auth-headers "access_token" %))
   :body))

(defn get-user-info [request]
  (my-oauth2-wrap "https://www.strava.com/api/v3/athlete" request "get"))

(defn get-athletes-clubs [request]
  (my-oauth2-wrap athlete-clubs request "get"))

(defroutes oauth2
  (GET "/" [] (get-page "index.html"))
  (GET "/map" [] (get-page "map.html"))
  (GET "/logout" request (assoc (response/redirect "/") :session {}))
  (GET "/activities" [] (fn [request] (get-polylines request)))
  (GET "/strava-user-info" request (get-user-info request))
  (GET "/clubs" request (get-athletes-clubs request))
  (GET "/strava-callback" request
       (oauth2-ring/oauth2-callback-handler oauth2-config request))
  (GET "/heat" [] (get-page "heatmap.html"))
  (GET "/leaflet-heatmap" [] (get-page "leaflet-heatmap.js"))
  (GET "/heatmap.js" [] (get-page "heatmap.js"))
  (GET "/heatdata" [] (testi/matching-coords testi/coords))
  (route/not-found "Not found now"))


(def handler
  (-> #'oauth2
      (wrap-session)
      (wrap-params)))

(defn -main
  [& args]
  (ring-jetty/run-jetty #'handler {:join? false :port 3000}))

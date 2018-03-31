(ns strava-server.utils
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def strava-api-url "https://www.strava.com/api/v3")
(def personal-token "")
(def options {:headers {"Authorization:" (str "Bearer " personal-token) }})

(def athlete-url (str strava-api-url "/athlete"))
(def activities-url (str strava-api-url "/athlete/activities?per_page=150"))



(defn download-page [url opts]
  (let [{:keys [status headers body error] :as resp} @(http/get url opts)]
    (if error
      error
      body)))

(defn download-n-pages [url opts num]
  (let [{:keys [status headers body error] :as resp} @(http/get url opts)]
    (if error
      error
      body)))


(defn get-data [url opts]
  (json/read-json (download-page url opts)))

(defn get-polylines [activities]
  (map #(-> (:map %) (:summary_polyline) activities)))

(defn get-activity-data [activities]
  (let [ tmps (map #(select-keys % [:name :map]) activities) ]
    (for [ tmp tmps ]
      { :name (:name tmp ) :summary_polyline (:summary_polyline (:map tmp))})))
  
(defn activity-data->json [activities]
  (json/write-str activities))
                  


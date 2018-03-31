(ns strava-server.activities
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def strava-api-url "https://www.strava.com/api/v3")
(def strava-key-new "3aec60290172a435b76079082216f9bb6d8798f0")
(def options {:headers {"Authorization:" (str "Bearer " strava-key-new) }})
(def activities-url (str strava-api-url "/athlete/activities?per_page=:num"))
(def activity-url (str strava-api-url "/activities/:id?include_all_efforts=true"))
(def segment-search (str strava-api-url "/segments/search?keywords=Tampere&filter_type=cycling"))

(defn download-page [url opts]
  (let [{:keys [status headers body error] :as resp} @(http/get url opts)]
    (if error
      error
      body)))

(defn get-data [url opts]
  (json/read-json (download-page url opts)))


(defn get-segments-from-activity [id]
  (let [url (clojure.string/replace activity-url #":id" (str id))
        act (get-data url options) ]
    (map #(:id %)(map #(:segment %)(:segment_efforts act)))))

(defn get-activity-ids [num]
  (let [url (clojure.string/replace activities-url #":num" (str num))
        acts (get-data url options) ]
    (->> acts
        (filter #(= (:type %) "Ride"))
        (map #(:id %)))))
  
(defn get-segment-ids-from-activities [num]
  (let [acts (get-activity-ids num)]
    (flatten (map #(get-segments-from-activity %) acts))))


(defn flatten-list [lists]
  (loop [my-lists lists
         target [] ]
    (if (= (count my-lists) 0)
      target
      (recur (rest my-lists) (concat target (first my-lists))))))

(defn increase-count [stats rider-key]
  (if (contains? stats rider-key)
    (assoc stats rider-key (inc (rider-key stats 0)))
    (assoc stats rider-key 1)))
                      
(defn leader-count [memo leader]
  (let [pos (keyword (str (second leader)))
        rider-key (keyword (clojure.string/replace (first leader) #" " "-")) ]
    (if (not (contains? memo pos))
      (assoc memo pos {rider-key 1})
      (assoc memo pos (increase-count (pos memo) rider-key)))))

(defn generate-leader-table [leaders]
  (reduce leader-count {} leaders))


(defn- get-keys [item wanted-keys]
  (into [] (map #(get item %) wanted-keys)))

(defn get-segment-leaderboard [id num]
  (let [strava-url (str strava-api-url "/segments/"id"/leaderboard") 
        leaderboard (get (get-data strava-url options) :entries)]
    (take num (map #(get-keys % [:athlete_name :rank]) leaderboard))))


(defn get-segment-leaderboard-async [segments]
  (let [future-list (doall (map #(future (get-segment-leaderboard % 10)) segments))
        contents (map #(deref % 2000 []) future-list) ]
  (into [] (for [c contents] c))))

(defn get-segment-leaderboards [segments]
  (let [segment-leaders (get-segment-leaderboard-async segments) ]
    (flatten-list (into [] (for [leader segment-leaders] leader)))))

(defn generate-toplist [num-of-acts]
  (let [segs (get-segment-ids-from-activities num-of-acts)
        lbs (get-segment-leaderboards segs)]
    (println (str "segment-count: " (count segs)))
    (generate-leader-table lbs)))

(defn show-sorted [entry]
  (reverse (sort-by last entry)))

(defn show-rider-results [rider table]
  (for [key (range 1 11)] (get-in table [(keyword (str key)) (keyword rider)] )))

(comment
  (let [segs (get-segment-ids-from-activities 5)
        lbs (get-segment-leaderboards segs)]
    (generate-leader-table lbs)))
    
;; (defn- coordinate->string [coord]
;;   (str (first coord) "," (second coord)))

;; (defn get-segments [nw se]
;;   (let [coord-string (str (coordinate->string nw) "," (coordinate->string se))
;;         strava-url (str strava-api-url "/segments/explore?bounds="coord-string"&activity_type=cycling")
;;         segments (get-data strava-url options) ]
;;     (map #(get-keys % [:id :name]) (get segments :segments))))

;; (defn leader-count-tmp [memo leader]
;;   (let [key (str (first leader)"-"(second leader))]
;;     (assoc memo key (inc (memo key 0)))))

;; (defn generate-leader-table-tmp [leaders]
;;   (reduce leader-count {} leaders))

;;  activity id = 1471013607
;; (def segment-url (str strava-api-url "/segments/explore?bounds=60.7994,22.4478,61.5933,25.0790&activity_type=cycling"))
;; (def kauppi-sw [61.4985 23.8225])
;; (def kauppi-ne [61.5230 23.9047])

;;     https://www.strava.com/segments/explore/search?bounds=61.4985%2C23.8225%2C61.5230%2C23.9047&zoom=14&min_cat=0&max_cat=5&activity_type=cycling&_=1521996632869


;; ;(def segment-url (str strava-api-url "/segments/explore&bounds=60.7994,22.4478%2C61.5933%2C25.0790&zoom=9&min_cat=0&max_cat=5&activity_type=cycling&_=1521996632851"))

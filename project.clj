(defproject strava_server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
		 [http-kit "2.2.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ddellacosta/oauth2-client "0.2.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler strava-server.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :uberjar {:aot :all}}
  :main ^:skip-aot strava-server.core
  :target-path "target/%s")



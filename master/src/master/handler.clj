(ns master.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [master.routes.home :refer [home-routes]]
            [master.routes.movie :refer [movie-routes]]
            [master.routes.comments :refer [comment-routes]]
            [master.models.db :as db]
            [noir.session :as session]
            [ring.middleware.session.memory :refer [memory-store]]
            [noir.validation :refer [wrap-noir-validation]]))

(defn init []
  (println "Master site is starting")
  (if-not (.exists (java.io.File. "./db.sq3"))
    (db/create-comment-table))
  (if-not (.exists (java.io.File. "./db.sq3"))
    (db/create-words-table)))


(defn destroy []
  (println "Master site is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Page Not Found"))

(def app
  (->
    (handler/site
      (routes comment-routes 
              movie-routes 
              home-routes
              app-routes))
    (wrap-base-url)
    (session/wrap-noir-session
      {:store (memory-store)})
    (wrap-noir-validation)))


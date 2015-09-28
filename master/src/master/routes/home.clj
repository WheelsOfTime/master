(ns master.routes.home
  (:require [compojure.core :refer :all]
            [master.views.layout :as layout]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [master.routes.movie :as movie]))

(defn home [name]
  (layout/common
    [:div#canvas
     [:div#header
      [:h1 "Movie search "]
      [:h2 "Welcome to movie search and comment web site!"]]
     [:hr]
     [:div#work (form-to [:post "/movie"]
                         [:p.comment "If you want to find movie you have to enter exact movie name."]
                         [:hr]
                         [:br]
                         [:p "Enter movie name:  " (text-field "name" name)]
                         [:br]
                         (submit-button {:class "button"} "Search"))
      [:br]
      [:hr]
      [:br]
      [:p#italic "Type movie name like: The Green Mile; Bad Boys; The Notebook; Taxi or any movie from iMDB."]]]
    ))


(defroutes home-routes
  (GET "/" [] (home nil))
  (POST "/movie" [name]))



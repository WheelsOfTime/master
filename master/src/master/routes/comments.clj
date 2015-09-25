(ns master.routes.comments
  (:require [compojure.core :refer :all]
            [master.views.layout :as layout]
            [master.models.db :as db]
            [hiccup.form :refer :all]))

(defn comment-page [title]
  (layout/common
    [:div#canvas1
     [:div#header
      [:h1 (str "Movie: " title)]
     (form-to [:get "/"]
              (submit-button {:class "button"} "Back to Search"))]
     [:hr]
     (for [{:keys [text name timestamp]} (db/read-comments title)]
       [:div#whole-comment
        [:p#one-comment text]
        [:p#signature name]]
       )]))

(defn comment-add [my-comment username title]
  (do
    (db/insert-comment username my-comment title)
    (comment-page title))
  )

(defroutes comment-routes
  (POST "/comments" [title] (comment-page title))
  (POST "/comments-add" [my-comment username title] (comment-add my-comment username title)))
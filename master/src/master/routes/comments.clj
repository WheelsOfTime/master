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
     (for [{:keys [id text username timestamp badcount goodcount spam]} (db/read-comments title)]
       
       [:div#whole-comment
        [:p#one-comment text]
        (form-to {:enctype "multipart/form-data"}
                 [:post "/comment-update"]
                 [:p#hidden (text-field "id" id)]
                 [:p#hidden (text-field "username" username)]
                 [:p#hidden (text-field "text" text)]
                 [:p#hidden (text-field "timestamp" timestamp)]
                 [:p#hidden (text-field "badcount" badcount)]
                 [:p#hidden (text-field "goodcount" goodcount)]
                 [:p#hidden (text-field "spam" spam)]
                 [:p#hidden (text-field "title" title)]
                 (submit-button {:class "btn" :name "submit"} "Like")                 
                 (submit-button {:class "btn" :name "submit"} "Spam"))
        (if (= spam 1) 
          [:p#spam (str "Likes: " goodcount " Dislikes: " badcount " Author: " username)]
          [:p#good (str "Likes: " goodcount " Dislikes: " badcount " Author: " username)])
        ])]))

(defn comment-add [my-comment username title]
  (do
    (db/insert-comment username my-comment title)
    (comment-page title)))

(defn make-comment [id text username timestamp badcount goodcount spam]
  {:id id :text text :username username :timestamp timestamp :badcount badcount :goodcount goodcount :spam spam})

(defn make-commentNo [id text username timestamp badcount goodcount spam]
  (map id text username timestamp badcount goodcount spam))

(defn like [button-clicked like-counter]
  (if (= button-clicked "Like")
    (inc like-counter) like-counter))

(defn dislike [button-clicked dislike-counter]
  (if (= button-clicked "Spam")
    (inc dislike-counter) dislike-counter))

(defn on-clik-spam [like-counter dislike-counter]
  (if (< like-counter dislike-counter)
    1 0))

(defn update-comment [map-data]
  (do
    (db/update-comment-db 
      (make-comment (get map-data "id") 
                    (get map-data "text") 
                    (get map-data "username") 
                    (get map-data "timestamp") 
                    (dislike 
                      (get map-data "submit")
                      (Integer/parseInt 
                        (get map-data "badcount")))
                    (like 
                      (get map-data "submit")
                      (Integer/parseInt 
                        (get map-data "goodcount")))
                    (on-clik-spam 
                      (like 
                        (get map-data "submit")
                        (Integer/parseInt 
                          (get map-data "goodcount")))
                      (dislike 
                        (get map-data "submit")
                        (Integer/parseInt 
                          (get map-data "badcount")))))
                    (get map-data "id"))
      (comment-page (get map-data "title"))))
  
  (defn update-commentNo [map]
    (layout/common
      [:div#canvas
       [:p 
        (get map "submit")]]))
  
  (defroutes comment-routes
    (POST "/comments" [title] 
          (comment-page title))
    (POST "/comments-add" [my-comment username title] 
          (comment-add my-comment username title))
    (POST "/comment-update" [:as request] 
          (update-comment (request :multipart-params))))
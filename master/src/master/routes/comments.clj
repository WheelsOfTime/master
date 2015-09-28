(ns master.routes.comments
  (:require [compojure.core :refer :all]
            [master.views.layout :as layout]
            [master.models.db :as db]
            [master.spam_helper :as spam]
            [hiccup.form :refer :all]))

(defn comment-page [title]
  (layout/common
    [:div#canvas1
     [:div#header
      [:h1 (str "Movie: " title)]
      (form-to [:get "/"]
               (submit-button {:class "button"} "Back to Search"))]
     [:hr]
     (for [{:keys [id text username timestamp badcount goodcount spam]} 
           (db/read-comments title)]
       
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
          [:p#spam 
           (str "Likes: " goodcount " Dislikes: " badcount "&ensp;&ensp; Author: " username)]
          [:p#good 
           (str "Likes: " goodcount " Dislikes: " badcount "&ensp;&ensp; Author: " username)])
        ])]))

(def classifier 
  (spam/make-classifier :good :bad))

(def spam-counter 0)

(defn text-to-list [text]
  (distinct 
    (re-seq #"\w+" 
            (clojure.string/lower-case text))))

(defn check-word [word-comm]
  (for [{:keys [word]} 
        (db/read-all-spam-words)]
    (if 
      (= word word-comm)
      (def spam-counter (inc spam-counter)))))

(defn check-comment [my-comment]
  (map check-word (text-to-list my-comment)))

(defn checked-data-insert [my-comment username title]
  (let [meth-add-1 (check-comment my-comment)]
    (str meth-add-1))
  (db/insert-comment username my-comment title 
                     (if 
                       (> spam-counter 2) 
                       1 0)))

(defn comment-add [my-comment username title]
     (do
    (checked-data-insert my-comment username  title)
    (comment-page title)))

(defn make-comment [id text username timestamp badcount goodcount spam]
  {:id id :text text :username username 
   :timestamp timestamp :badcount badcount :goodcount goodcount :spam spam})

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

(defn fill-classifier-bad [word]
  (if 
    (> (count word) 2)
    (do
      (def classifier 
        (spam/ntrain! classifier word :bad))
      (def classifier
        (spam/set-zero classifier word :good))
      )))

(defn fill-classifier-good [word]
  (if 
    (> (count word) 2)
    (do
      (def classifier 
        (spam/ntrain! classifier word :good))
      (def classifier 
        (spam/set-zero classifier word :bad))
      )))

(defn make-word-map [word badcount goodcount probability]
  {:word word :badcount badcount :goodcount goodcount :probability probability})

(defn insert-or-update [word]
  (if (> (count word) 2)
    (if (= nil (db/read-word word))
      (db/insert-word 
        word 
        (get (->> classifier
               :categories
               :bad
               :features
               ) word )
        (get (->> classifier
               :categories
               :good
               :features
               ) word )
        (:bad (spam/np-of-category-given-feature classifier word)))
      (db/update-word-db 
        (make-word-map 
          word 
          (get (->> classifier
                 :categories
                 :bad
                 :features
                 ) word )
          (get (->> classifier
                 :categories
                 :good
                 :features
                 ) word )
          (:bad (spam/np-of-category-given-feature classifier word)))
        word))))

;;update or insert
(defn get-probability-and-db [comment-text]
  (map insert-or-update (text-to-list comment-text)))


;(defn words-do-all [comment-text is-spam]
;  (do
;    (words-do-all-for-classifier comment-text is-spam)
;    (get-probability-and-db comment-text)))

;(defn words-do-all-for-classifier [comment-text is-spam]
;  (do
;    (insert-into-classifier comment-text is-spam)
;    (map get-word-data-from-db (text-to-list comment-text))))



(defn call-method [comment-text is-spam & meth]
  (get-probability-and-db comment-text))


;;napunjen classifier i iz baze FALI (map ovaFN mapa)
(defn get-word-data-from-db [word]
  (if (> (count word) 2)
    (for [{:keys [word badcount goodcount probability]} 
          (db/read-word word)]
      (do 
        (def classifier (spam/update-from-db-num classifier word :bad (if (= badcount nil) 0 badcount)))
        (def classifier (spam/update-from-db-num classifier word :good (if (= goodcount nil) 0 goodcount)))
        ))))

(defn get-word-data-from-db-to-classifier [word]
  (for [{:keys [word badcount goodcount probability]} 
        (db/read-word word)]
    (let [do-one (spam/update-from-db-num classifier word :bad (if (= badcount nil) 0 badcount))
          do-sec (spam/update-from-db-num classifier word :good (if (= goodcount nil) 0 goodcount))]
      (str do-one do-sec))))

;;prvo ubacivanje u classifier - value 1
(defn insert-into-classifier [comment-text is-bad]
  (map (if (= is-bad 1) 
         fill-classifier-bad 
         fill-classifier-good) 
       (text-to-list comment-text)))



(defn call-method-all [comment-text is-spam]
  (let [meth1 (insert-into-classifier comment-text is-spam)
        meth2 (map get-word-data-from-db (text-to-list comment-text))
        meth3 (get-probability-and-db comment-text)]
    (str meth1 meth2 meth3 ))
  (def classifier 
    (spam/make-classifier :good :bad)))

(defn update-comment [map-data]
  (let [is-spam (on-clik-spam 
                  (like 
                    (get map-data "submit")
                    (Integer/parseInt 
                      (get map-data "goodcount")))
                  (dislike 
                    (get map-data "submit")
                    (Integer/parseInt 
                      (get map-data "badcount"))))]
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
                      is-spam)
        (get map-data "id"))       
      (call-method-all 
        (get map-data "text") 
        (if (= (get map-data "submit") "Spam") 
          1 0))))
  (comment-page (get map-data "title")))

(defroutes comment-routes
  (POST "/comments" [title] 
        (comment-page title))
  (POST "/comments-add" [my-comment username title]
        (def spam-counter 0)
        (comment-add my-comment username title))
  (POST "/comment-update" [:as request] 
        (update-comment (request :multipart-params))))
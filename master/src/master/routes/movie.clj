(ns master.routes.movie 
  (:require [compojure.core :refer :all]
            [master.views.layout :as layout]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [clj-http.client :as client]
            [clojure.xml :as xml]))

(def url-part-1 "http://www.omdbapi.com/?t=")
(def url-part-2 "&y=&plot=full&r=xml")

(defn get-full-url [name]
  (str url-part-1 
       (clojure.string/replace 
         (clojure.string/trim name) " " "+") url-part-2))

(def username nil)
(def my-comment nil)
(def title nil)


(defn http-call
  [url]
  (:body (client/get url)))

(defn parse [xmlData]
  (clojure.xml/parse
    (java.io.ByteArrayInputStream. (.getBytes xmlData))))

(defn get-from-imdb [name]
  name)

(defn set-data-on-page [data]
  (if (= (->> data
           :content
           first
           :attrs
           :title
           ) nil) 
    (layout/common
      [:div#canvas
       [:div#header 
        [:h1 "Movie with that name does not exsist on iMDB."]
        [:h1 "Go back and search again!"]
        [:hr]
        (form-to [:get "/"]
                 [:br]
                 (submit-button {:class "button"} "Go back!"))]])
    (do
      (def title (->> data
                   :content
                   first
                   :attrs
                   :title
                   ))
      (layout/common
        [:div#canvas1
         [:div#header
          [:h1 (str "Movie name: " (->> data
                                     :content
                                     first
                                     :attrs
                                     :title
                                     ))]      
          [:hr]
          [:p.lead "Lead Actors:" ]
          [:p (->> data
                :content
                first
                :attrs
                :actors
                )]
          
          (image  (->> data
                    :content
                    first
                    :attrs
                    :poster
                    ))
          [:p.lead "Director:" ]
          [:p (->> data
                :content
                first
                :attrs
                :director
                )]
          
          [:p.lead "Plot:" ]
          [:p#plot (->> data
                     :content
                     first
                     :attrs
                     :plot
                     )]
          [:p.lead "Rating:" ]
          [:p (->> data
                :content
                first
                :attrs
                :imdbRating
                )]
          [:p.lead "Genre:" ]
          [:p (->> data
                :content
                first
                :attrs
                :genre
                )]
          [:p.lead "Writer(s):" ]
          [:p (->> data
                :content
                first
                :attrs
                :writer
                )]
          [:hr]
          (form-to [:post "/comments-add"]
                   [:p#hidden (text-field "title" title)]
                   [:p#comment "Username:"]
                   [:p (text-field "username" username)]
                   [:p#comment "Enter your comment about this movie:" ]
                   [:p (text-area {:rows 8 :cols 75} "my-comment" my-comment)]
                   (submit-button {:class "button"} "Comment" ))
          (form-to [:post "/comments"]
                   [:p#hidden (text-field "title" title)]
                   [:br]
                   (submit-button {:class "button"} "All comments"))
          [:br]]]))))

(defn movie-page [name]
  (layout/common   
    (set-data-on-page 
      (parse 
        (http-call 
          (get-full-url name))))))

(defroutes movie-routes
  (POST "/movie" [name] (movie-page name))
  (POST "/comments" [title])
  (POST "/comments-add" [my-comment username title]))

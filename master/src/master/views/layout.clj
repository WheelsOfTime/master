(ns master.views.layout
    (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Movie comment"]
     (include-css "/css/master.css")]
    [:body body]))



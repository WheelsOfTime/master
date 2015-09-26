(ns master.models.db
  (:require [clojure.java.jdbc :as sql])
  (:import java.sql.DriverManager))

(def db {:classname "org.sqlite.JDBC",
         :subprotocol "sqlite",
         :subname "db.sq3"})

(defn create-comment-table []
  (sql/with-connection
    db
    (sql/create-table
      :comment
      [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
      [:timestamp "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"]
      [:text "TEXT"]
      [:username "TEXT"]
      [:title "TEXT"]
      [:badcount "INTEGER"]
      [:goodcount "INTEGER"]
      [:spam "INTEGER"])
    (sql/do-commands "CREATE INDEX timestamp_index ON comment (timestamp)")))

(defn read-comments [title]
  (sql/with-connection
    db
    (sql/with-query-results res
      [(str "SELECT * FROM comment WHERE title = '" title "' ORDER BY spam ASC, goodcount DESC")]
      (doall res))))

(defn insert-comment [name comment-text title]
  (sql/with-connection
    db
    (sql/insert-values
      :comment
      [:username :text :timestamp :title :goodCount :badCount :spam]
      [name comment-text (new java.util.Date) title 0 0 0])))

(defn update-comment-db [comment id]
  (sql/with-connection
    db
    (sql/update-values
      :comment
      ["id=?" id]
      comment)))

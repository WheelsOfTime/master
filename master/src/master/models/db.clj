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

(defn create-words-table []
  (sql/with-connection
    db
    (sql/create-table
      :words     
      [:word "TEXT"]   
      [:badcount "INTEGER"]
      [:goodcount "INTEGER"]
      [:probability "REAL"])
    (sql/do-commands "CREATE INDEX word_index ON words (word)")))

(defn read-comments [title]
  (sql/with-connection
    db
    (sql/with-query-results res
      [(str "SELECT * FROM comment WHERE title = '" title "' ORDER BY spam ASC, goodcount DESC, timestamp DESC")]
      (doall res))))

(defn read-word [word]
  (sql/with-connection
    db
    (sql/with-query-results res
      [(str "SELECT * FROM words WHERE word = '" word "'")]
      (doall res))))

(defn read-all-words []
  (sql/with-connection
    db
    (sql/with-query-results res
      [(str "SELECT * FROM words")]
      (doall res))))

(defn read-all-spam-words []
  (sql/with-connection
    db
    (sql/with-query-results res
      [(str "SELECT word FROM words WHERE probability > 0.9 AND probability < 1.0")]
      (doall res))))

(defn insert-comment [name comment-text title spam]
  (sql/with-connection
    db
    (sql/insert-values
      :comment
      [:username :text :timestamp :title :goodCount :badCount :spam]
      [name comment-text (new java.util.Date) title 0 0 spam])))

(defn insert-word [word badcount goodcount probability]
  (sql/with-connection
    db
    (sql/insert-values
      :words
      [:word :badcount :goodcount :probability]
      [word badcount goodcount probability])))

(defn update-comment-db [comment id]
  (sql/with-connection
    db
    (sql/update-values
      :comment
      ["id=?" id]
      comment)))

(defn update-word-db [word wordid]
  (sql/with-connection
    db
    (sql/update-values
      :words
      ["word=?" wordid]
      word)))


(defn delete-word [word]
  (sql/with-connection
    db
    (sql/delete-rows
      :words
      ["word=?" word])))


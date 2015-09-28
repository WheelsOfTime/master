UPUTSTVO

Sledeca aplikacija je napravljena za ispit iz predmeta Alati i metode softverskog inženjerstva.
Napisana je u programskom jeziku Clojure.
Ideja aplikacije je komentarisanje filmova.
Aplikcija je povezana sa sajtom IMDB.com.
Sve podatke o filmovima pa i sam naziv po kome se nalazi film se "skidaju" sa tog sajta.
Komentari za svaki film se čuva u SQLite bazi ovog sajta.


Aplikacija je ravljena u Eclipse-u pomocu plug-ina Counterclockwise.
Sastoji se od glavnog foldera master koji u sebi sadrži i ostale foldere.
-models
    u ovom folderu se nalaze klase za upravljanje  bazom podataka ili modeli stvarnih objekata prikazanih prko npr. mapa 
-routes
    u rutama se nalaze stranice koje omogućavaju rutiranje i kretanje preko stranica.
-views
    u ovom delu se nalaze opšt izgledi stranica- layouti ili template-i
    
Postoje jos dva važna fajla:
- handler.clj - on služi da upravlja pozivima (requests) i odgovorima (responses) u aplikaciji
- repl.clj - je fajl koji služi da se aplikacija pokrene u development modu.

Dakle, aplikacija se pokreće kada se u pokrenutom REPL-u ukuca sledeći kod
--> (use 'master.repl)
kada se ovo učita onda se kuca:
--> (start-server)
i automatski bi trebalo da se pokrene aplikacija na početnoj stranici.

NAPOMENA: Ukoliko je port vec zauzet u metodi start-server namespace-a repl.clj je moguće izmeniti trenutno dodeljeni port

Takođe, pre početka je potrebno kreirati tabele u bazi: comment i words:.
U bazi db.sq3 koja je na github-u su te kreirane i ne postoji nijedan podatak u tabelama.

Kreira se na sledeći način:
--> (use 'master.models.db)
--> (create-words-table)
--> (create-comment-table)
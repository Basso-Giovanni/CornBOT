# Analisi: Project Work “Bot Telegram in Java”

Materia: Tecnologie e Progettazione di Sistemi Informatici
Autore: Basso Giovanni, 5BII

# CornBOT

![logo_cornbot](https://github.com/user-attachments/assets/068f1f70-e5ea-4e33-837c-8ab052f2952b)


# Introduzione

Questo documento definisce un'analisi dettagliata per lo sviluppo di un bot di Telegram che
fornisce informazioni riguardanti film e attori. Il bot recupera i dati da un database MySQL , che
viene popolato, estraendo tramite un WebScraper, informazioni dai siti come [https://www.imdb.com/it/](https://www.imdb.com/it/) o [https://www.rottentomatoes.com/](https://www.rottentomatoes.com/).

# Contesto

Facilitare la ricerca di informazioni su film. Aiutare gli utenti a scoprire nuovi generi o recuperare vecchie pellicole. 

# Obiettivi del Software

Fornire un nuovo modo per assimilare informazioni su film in modo rapido e facile. L’utente in modo interattivo può cercare le informazioni, eseguire recensioni, aggiungere nuovi elementi alla watchlist o vedere i film disponibili nei cinema nelle vicinanze.

# Requisiti di sistema

Il bot deve essere in grado di rispondere in breve tempo con informazioni verificate. Inoltre deve essere disponibile e utilizzabile da più utenti in contemporanea.

# Funzionalità

- Ricerca informazioni sul film partendo da un titolo
    
    Informazioni restituite:
    
    - Informazioni di base (titolo, durata, anno di produzione, genere, …)
    - Trama
    - Dove guardarlo (Netflix, Amazon Prime Video, …)
    - Recensioni
    - Trailer
- Ricerca informazioni su registi/attori
    
    Informazioni restituite:
    
    - Dati anagrafici
    - Biografia
    - Filmografia
- Possibilità di vedere i film disponibili nei cinema nelle vicinanze
- Possibilità di scrivere appunti su film/attori/registi
- Possibilità salvare dei registi/attori preferiti o film
- Possibilità di segnarsi nel calendario personale la data di uscita di un film

# Tecnologie
![97a9f158-dafa-4655-843a-597b79100f17](https://github.com/user-attachments/assets/c6d95bda-1261-495f-8724-d7c5f2b9ea47)

Linguaggi di Programmazione: Java per la logica del bot e per l'estrazione dei dati.
Database Management System (DBMS): MySQL per la gestione del database.
API Telegram: Utilizzata per la comunicazione con gli utenti e la gestione dei comandi.


# Librerie

TelegramBots java: Libreria Java per interagire con l'API di Telegram.
[https://github.com/rubenlagus/TelegramBots?authuser=0](https://github.com/rubenlagus/TelegramBots?authuser=0)
Java JSoup: Utilizzate per l'estrazione di dati (web scraping) dai siti indicati.
[https://jsoup.org/](https://jsoup.org/)

# Database

Il database ha lo scopo di mantenere le informazioni in tabelle relazionali. Saranno presenti diverse tabelle in base all’informazione che si vuole archiviare.

# Modello E-R (Entità-Relazioni)
![1e5b6fbd-3b9e-4a74-b0d0-888885c1e47c](https://github.com/user-attachments/assets/f7a806b1-0b40-4ea7-85a6-7bf5b1ea5cb0)


[https://lucid.app/lucidchart/edbb891d-0a2b-46cd-a4a7-9283ad21f03e/edit?invitationId=inv_a36c94a3-a5bb-41cf-89d6-cdf578b57484](https://lucid.app/lucidchart/edbb891d-0a2b-46cd-a4a7-9283ad21f03e/edit?invitationId=inv_a36c94a3-a5bb-41cf-89d6-cdf578b57484)

# Modello logico relazionale

`Soggetto`

(id_soggetto, nome, data_nascita, luogo_nascita, data_morte, luogo_morte, sesso, biografia)

---

`Film`

(id_film, titolo, durata, anno_produzione, genere, trama, piattaforme, trailer_url, data_uscita, regista*)

`FOREIGN KEY` regista `REFERENCES` Soggetto.id_soggetto

---

`Partecipare`

(id_partecipare, film*, soggetto*, personaggio)

`FOREIGN KEY` film `REFERENCES` Film.id_film

`FOREIGN KEY` soggetto `REFERENCES` Soggetto.id_soggetto

---

`Utente`

(id_utente, telegram_id)

---

`Cinema`

(id_cinema, nome, indirizzo, citta, telefono, sito)

---

`Recensione`

(id_recensione, data, testo, rating, utente*, film*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` film `REFERENCES` Film.id_film

---

`Appunti`

(id_appunto, contenuto, utente*, film*, soggetto*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` film `REFERENCES` Film.id_film

`FOREIGN KEY` soggetto `REFERENCES` Soggetto.id_soggetto

---

`Watchlist`

(utente*, film*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` film `REFERENCES` Film.id_film

---

`Proiettare`

(cinema*, film*)

`FOREIGN KEY` cinema `REFERENCES` Cinema.id_cinema

`FOREIGN KEY` film `REFERENCES` Film.id_film

---

`Preferiti_film`

(utente*, film*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` film `REFERENCES` Film.id_film

---

`Preferiti_soggetti`

(utente*, soggetto*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` soggetto `REFERENCES` Soggetto.id_soggetto

---

`Promemoria`

(utente*, film*)

`FOREIGN KEY` utente `REFERENCES` Utente.id_utente

`FOREIGN KEY` film `REFERENCES` Film.id_film

# SQL

```sql
-- Creazione del database
CREATE DATABASE CornBOT_DB;
USE CornBOT_DB;

-- Tabella Soggetto
CREATE TABLE Soggetto 
(
    id_soggetto INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
    data_nascita DATE,
    luogo_nascita VARCHAR(255),
    data_morte DATE,
    luogo_morte VARCHAR(255),
    sesso VARCHAR(100),
    biografia TEXT
);

-- Tabella Film
CREATE TABLE Film 
(
    id_film INT AUTO_INCREMENT PRIMARY KEY,
    titolo VARCHAR(255) UNIQUE NOT NULL,
    durata INT NOT NULL,
    anno_produzione YEAR NOT NULL,
    genere VARCHAR(100),
    trama TEXT,
    piattaforme VARCHAR(255),
    trailer_url VARCHAR(255),
    data_uscita DATE,
    regista INT,
    FOREIGN KEY (regista) REFERENCES Soggetto(id_soggetto) ON DELETE SET NULL
);

-- Tabella Partecipare
CREATE TABLE Partecipare
(
    id_partecipare INT AUTO_INCREMENT PRIMARY KEY,
    film INT NOT NULL,
    soggetto INT NOT NULL,
    ruolo VARCHAR(255),
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE,
    FOREIGN KEY (soggetto) REFERENCES soggetto(id_soggetto) ON DELETE CASCADE
);

-- Tabella Cinema
CREATE TABLE Cinema 
(
    id_cinema INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    indirizzo VARCHAR(255),
    città VARCHAR(100),
    telefono VARCHAR(15)
);

-- Tabella Utente
CREATE TABLE Utente 
(
    id_utente INT AUTO_INCREMENT PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE
);

-- Tabella Recensione
CREATE TABLE Recensione
(
    id_recensione INT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    testo TEXT,
    rating INT NOT NULL,
    utente INT NOT NULL,
    film INT NOT NULL,
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE
);

-- Tabella Riconoscimento
CREATE TABLE Riconoscimento
(
    id_riconoscimento INT AUTO_INCREMENT PRIMARY KEY,
    tipo_riconoscimento VARCHAR(255),
    data_premiazione DATE,
    luogo_premiazione VARCHAR(255),
    film INT NOT NULL,
    soggetto INT NULL,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE,
    FOREIGN KEY (soggetto) REFERENCES Soggetto(id_soggetto) ON DELETE CASCADE
);

-- Tabella Appunti
CREATE TABLE Appunti 
(
    id_appunto INT AUTO_INCREMENT PRIMARY KEY,
    utente INT,
    film INT NULL,
    soggetto INT NULL,
    contenuto TEXT NOT NULL,
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE SET NULL,
    FOREIGN KEY (soggetto) REFERENCES Soggetto(id_soggetto) ON DELETE SET NULL
);

-- Tabella Watchlist
CREATE TABLE Watchlist
(
    utente INT,
    film INT,
    PRIMARY KEY (utente, film),
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE
);

-- Tabella di Proiettare
CREATE TABLE Proiettare
(
    cinema INT,
    film INT,
    PRIMARY KEY (cinema, film),
    FOREIGN KEY (cinema) REFERENCES Cinema(id_cinema) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE
);

-- Tabella Preferiti (film)
CREATE TABLE Preferiti_film 
(
    utente INT,
    film INT,
    PRIMARY KEY (utente, film),
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE
);

-- Tabella Preferiti (attori/registi)
CREATE TABLE Preferiti_soggetti 
(
    utente INT,
    soggetto INT,
    PRIMARY KEY (utente, soggetto),
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (soggetto) REFERENCES Soggetto(id_soggetto) ON DELETE CASCADE
);

-- Tabella Promemoria 
CREATE TABLE Promemoria 
(
    utente INT,
    film INT,
    PRIMARY KEY (utente, film),
    FOREIGN KEY (utente) REFERENCES Utente(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (film) REFERENCES Film(id_film) ON DELETE CASCADE
);
```

# Interfaccia utente

Avrà un’interfaccia molto semplice mantenendo lo stile Telegram. La risposta con le informazioni dei film mostreranno i dati principali in maniera schematica e in allegato la copertina o trailer. Dopo l’utente può chiedere informazioni specifiche come cast o trama. 



# Comandi

L’obiettivo è quello di usare meno comandi possibili per rendere l’interfaccia più chiara e facile da usare. Quelli essenziali sono:

`/start`: Avvia la conversazione con il bot e fornisce una descrizione delle sue funzionalità.

`/cercafilm [titolo]`: Cerca informazioni sul film specificato.

`/cerca [argomento]`: Cerca informazioni sull’argomento specificato (come il nome di un attore).

`/watchlist`: Visualizza i film presenti nella propria watchlist.

`/aggiungiwatchlist`: Aggiungi film alla watchlist (viene preso l’ultimo film cercato dall’utente).

`/visto [id_film]`: Rimuove il film dalla watchlist.

`/preferiti`: Mostra gli attori/registi preferiti.

`/aggiungipreferiti`: Aggiungi attore/regista ai preferiti.

Inoltre saranno presenti comandi secondari come `/help` per avere un elenco di comandi.

# Bookt

Bookt è un'app Android per la gestione e la scoperta di libri.  
L'app permette all'utente di cercare libri tramite Google Books API, visualizzarne i dettagli, salvarli nella propria libreria personale e ricevere consigli basati sui libri già letti o preferiti.

Il progetto è stato sviluppato come applicazione mobile Android in Kotlin, con interfaccia realizzata in Jetpack Compose e integrazione con servizi esterni tramite Retrofit e Firebase.

## Funzionalità principali

Bookt permette di:

- cercare libri tramite Google Books API;
- visualizzare una schermata di dettaglio per ogni libro;
- aggiungere un libro alla lista "Da leggere";
- aggiungere un libro ai "Preferiti";
- segnare un libro come "Letto";
- gestire automaticamente lo stato dei libri tra "Da leggere" e "Letti";
- registrarsi e accedere con un account personale;
- salvare la libreria utente su Firebase Firestore;
- usare un salvataggio locale quando l'utente non è autenticato;
- visualizzare gli ultimi libri preferiti e letti nella sezione Account;
- aprire le liste complete di Preferiti e Letti;
- ordinare Preferiti e Letti per ordine recente, autore o genere;
- ricevere consigli personalizzati basati sui libri salvati.

## Struttura dell'app

L'app è organizzata in tre sezioni principali:

### Esplora

La sezione Esplora permette di cercare libri tramite Google Books API.  
Sono presenti anche chip di ricerca rapida per alcuni generi, come fantasy, romanzi, thriller e manga.

Da questa sezione è possibile aprire la schermata di dettaglio di un libro e salvarlo nelle proprie liste.

### Libri

La sezione Libri contiene i libri salvati come "Da leggere".  
Ogni libro può essere aperto nella schermata di dettaglio, dove è possibile modificarne lo stato.

### Account

La sezione Account gestisce autenticazione e libreria personale.  
Se l'utente non è autenticato, vengono mostrati i campi per login e registrazione.  
Se l'utente è autenticato, vengono mostrati:

- email dell'account;
- pulsante di logout;
- ultimi libri preferiti;
- ultimi libri letti;
- accesso alle liste complete di Preferiti e Letti.

Le liste complete permettono anche l'ordinamento per:

- recenti;
- autore;
- genere.

## Schermata dettaglio libro

La schermata di dettaglio mostra le informazioni principali del libro:

- copertina;
- titolo;
- autore;
- categoria;
- descrizione;
- editore;
- data di pubblicazione;
- numero di pagine;
- rating, se disponibile.

Da questa schermata è possibile:

- aggiungere o rimuovere il libro da "Da leggere";
- aggiungere o rimuovere il libro dai "Preferiti";
- segnare o deselezionare il libro come "Letto".

Quando un libro viene segnato come letto, viene rimosso automaticamente dalla lista "Da leggere", in modo da mantenere coerente lo stato della libreria.

## Sistema di raccomandazione

La sezione "Consigliati" utilizza una logica di raccomandazione content-based.

Il sistema analizza i libri salvati dall'utente tra Preferiti e Letti, estraendo soprattutto:

- categorie;
- autori;
- metadati disponibili.

A partire da questi dati, l'app costruisce query verso Google Books API e ordina i risultati tramite uno score interno.  
Il punteggio considera elementi come:

- affinità di genere;
- affinità di autore;
- rating;
- presenza di copertina;
- qualità della descrizione;
- diversificazione degli autori.

Per evitare risultati troppo ripetitivi, il sistema limita il peso dell'autore e dà maggiore importanza alle categorie.  
Inoltre, i risultati vengono memorizzati temporaneamente in cache, così da evitare caricamenti ripetuti quando l'utente torna più volte sulla sezione Consigliati senza aver modificato la propria libreria.

Questa soluzione rappresenta una base per una possibile evoluzione futura con tecniche più avanzate, come clustering, embedding testuali o modelli di machine learning per la similarità semantica tra libri.

## Tecnologie utilizzate

Il progetto utilizza:

- Kotlin;
- Android SDK;
- Jetpack Compose;
- Material 3;
- Navigation Component;
- Retrofit;
- Google Books API;
- Firebase Authentication;
- Firebase Firestore;
- SharedPreferences per il salvataggio locale;
- Coil per il caricamento delle immagini;
- Gradle Kotlin DSL.

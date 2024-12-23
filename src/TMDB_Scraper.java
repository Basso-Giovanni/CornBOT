import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

//CLASSE PER LO SCRAPING DAL SITO TMDB
public class TMDB_Scraper
{
    /**
     * Metodo per cercare l'URL del film/soggetto partendo dal titolo/nome
     * @param titolo titolo del film/nome del soggetto
     * @param film se si tratta di un film o di un soggetto
     */
    public static void Ricerca(String titolo, boolean film)
    {
        try
        {
            try
            {
                Thread.sleep(2000); //provo a mettere un'attesa per risolvere il problema del timeout
            }
            catch (InterruptedException e)
            {
                System.out.println("Errore nellttesa del thread per la ricerca");
            }
            String URL = "https://themoviedb.org/search?query=";

            if (titolo != null && !titolo.isEmpty())
            {
                titolo.replace(" ", "+");
                Document doc = Jsoup.connect(URL + titolo).get();
                Element firstLink;
                if (film) //se si tratta di un film
                    firstLink = doc.selectFirst(".results .card .image a");
                else //o di un soggetto
                    firstLink = doc.selectFirst(".results .item.profile .image_content a.result");

                if (firstLink != null) //prend il primo link
                {
                    String href = firstLink.attr("href");
                    String risultato = "https://www.themoviedb.org" + href;
                    int id = Integer.valueOf(risultato.split("/")[4].split("-")[0]);
                    if (!risultato.contains("person"))
                        TMDB_Scraper.Film_scraper(risultato, id);
                    else
                        System.out.println("Elemento non trovato!");
                }
                else
                    System.out.println("Elemento non trovato!");
            }
        }
        catch (IOException e)
        {
            System.out.println("Errore nella ricerca dell'elemento");
        }
    }

    /**
     * Metodo per ottenere i dati del film
     * @param URL URL da cui effettuare lo scraping
     * @param id id TMDB del film
     */
    public static void Film_scraper(String URL, int id)
    {
        try
        {
            Document doc = Jsoup.connect(URL).get();
            String titolo = doc.select("h2 a").first().text();
            String anno = doc.select(".release_date").text().replaceAll("[^0-9]", "");
            String trama = doc.select(".overview p").text();
            String genere = doc.select(".genres a").text();
            String data_uscita = doc.select(".release").text();

            // convertire la data_uscita in formato DATE di MySQL
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(data_uscita.split(" ")[0], inputFormatter);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = date.format(outputFormatter);

            String durata = doc.select(".runtime").text();
            // convertire la durata in minuti
            int hours = 0, minutes = 0;

            if (durata.contains("h"))
            {
                String hoursPart = durata.split("h")[0].trim();
                hours = Integer.parseInt(hoursPart);
            }
            if (durata.contains("m"))
            {
                String minutesPart = durata.split("h")[1].split("m")[0].trim();
                minutes = Integer.parseInt(minutesPart);
            }

            int totalMinutes = (hours * 60) + minutes;

            ArrayList<String> piattaforme = TMDB_API.GET_piattaforme(id);
            StringBuilder sb = new StringBuilder();

            for (String p : piattaforme)
            {
                sb.append(p + " ");
            }

            String url_trailer = TMDB_API.GET_trailer(id); //ottengo le informazioni del trailer tramite API

            String regista = "";
            Elements people = doc.select("ol.people.no_image li.profile");

            for (Element person : people) //ricerca del regista
            {
                String role = person.select("p.character").text();
                if (role.contains("Director"))
                {
                    regista = person.select("p > a").text();
                    break;
                }
            }
            String sql_regista = "SELECT id_soggetto FROM Soggetto WHERE nome = ?";
            Integer registaId = DB_Manager.query_ID(sql_regista, regista);

            if (registaId == null) registaId = Soggetto_scraper(id, true, 0, regista);

            String sql = "INSERT IGNORE INTO Film (titolo, anno_produzione, genere, trama, durata, data_uscita, piattaforme, trailer_url, regista) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            DB_Manager.update(sql, titolo, anno, genere, trama, totalMinutes, formattedDate, sb.toString(), url_trailer, registaId);

            String query_id_film = "SELECT id_film FROM Film WHERE titolo = ?";
            Integer id_film = DB_Manager.query_ID(query_id_film, titolo);

            HashMap<Integer, String> ids = TMDB_API.GET_attoreIDDaFilm(id);

            for (Integer id_attore : ids.keySet()) //ricerca del cast
            {
                ArrayList<String> info_attore = TMDB_API.GET_soggetto(id_attore);
                if (info_attore != null)
                {
                    String sql_attore = "SELECT id_soggetto FROM Soggetto WHERE nome = ?";
                    Integer attoreId = DB_Manager.query_ID(sql_attore, info_attore.get(0));

                    if (attoreId == null) attoreId = Soggetto_scraper(id, false, id_attore, null);

                    String sql_attoreFilm = "INSERT INTO Partecipare (film, soggetto, ruolo) VALUES (?, ?, ?)";
                    DB_Manager.update(sql_attoreFilm, id_film, attoreId, ids.get(id_attore));
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nell'estrazione dei dati. URL: " + URL);
        }
    }

    /**
     * Metodo per effettuare lo scraping del soggetto
     * @param id_film id del film di partenza
     * @param regia se si tratta del regista o di qualcun altro
     * @param id_attore id TMDB del soggetto
     * @param nome nome del soggetto
     * @return id del soggetto
     */
    public static Integer Soggetto_scraper(int id_film, boolean regia, int id_attore, String nome)
    {
        try
        {
            ArrayList<String> info;
            if (regia)
                info = TMDB_API.GET_soggetto(TMDB_API.GET_registaIDDaFilm(id_film, nome));
            else
                info = TMDB_API.GET_soggetto(id_attore);

            // Inserire il soggetto nella tabella Soggetto
            String insertQuery = "INSERT INTO Soggetto (nome, data_nascita, luogo_nascita, data_morte, biografia, sesso) VALUES (?, ?, ?, ?, ?, ?)";
            DB_Manager.update(insertQuery, info.get(0), info.get(1), info.get(3), info.get(2), info.get(4), info.get(5));

            // Recupera l'ID del soggetto appena inserito
            String getIdQuery = "SELECT id_soggetto FROM Soggetto WHERE nome = ?";
            return DB_Manager.query_ID(getIdQuery, info.get(0));
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nello scraping del soggetto: " + id_attore + " del film " + id_film);
            return null;
        }
    }
}

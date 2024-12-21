import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CS_Scraper
{
    static String baseUrl = "https://www.comingsoon.it";

    public static void Cinema_scraping()
    {
        try
        {
            // 1. Collegati alla pagina principale
            Document doc = Jsoup.connect("https://www.comingsoon.it/cinema/vicenza/").get();

            // 2. Trova tutti i link relativi ai cinema
            Elements cinemaLinks = doc.select("a.cs-btn.secondary.tag"); // Seleziona i link con questa classe

            // 3. Itera su ciascun link
            for (Element link : cinemaLinks) {
                // Ottieni l'attributo 'href' e crea il link completo
                String cinemaUrl = baseUrl + link.attr("href");

                Scraper_cinema_singolo(cinemaUrl);
                //Scraper_DatiCinema(cinemaUrl);

                // Stampa il link
                System.out.println("Visitando: " + cinemaUrl);

                // 4. Apri la pagina del cinema
                try {
                    Document cinemaDoc = Jsoup.connect(cinemaUrl).get();

                    // Esempio: Estrai il nome del cinema o altre informazioni dalla pagina
                    String cinemaTitle = cinemaDoc.select("h1").text(); // Cambia il selettore con quello specifico
                    System.out.println("Titolo Cinema: " + cinemaTitle);

                    // Puoi aggiungere ulteriori estrazioni qui
                } catch (IOException e) {
                    System.out.println("Errore nel caricamento della pagina: " + cinemaUrl);
                }

                System.out.println("---------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Scraper_cinema_singolo(String cinemaUrl)
    {
        try
        {
            Document doc = Jsoup.connect(cinemaUrl).get();

            Elements films = doc.select(".row.rel"); // Selettore per ogni contenitore di film

            for (Element film : films)
            {
                String title = film.select(".tit_olo").text();
                String subtitle = film.select(".sottotitolo").text();
                String cinema = doc.select("h1.h1").text();

                try
                {
                    String sql_cinema = "SELECT id_cinema FROM cinema WHERE nome = ?";
                    String sql_film = "SELECT id_film FROM film WHERE titolo = ?";
                    Integer rs_cinema = DB_Manager.query_ID(sql_cinema, cinema);
                    Integer rs_film = DB_Manager.query_ID(sql_film, title);

                    if (rs_cinema != null && rs_film != null)
                    {
                        String sql = "INSERT INTO proiettare (film, cinema) VALUES (?, ?)";
                        DB_Manager.update(sql, rs_film, rs_cinema);
                    }
                }
                catch (SQLException e)
                {
                    System.out.println("Errore nell'inserimento della proiezione!");
                }
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void Scraper_DatiCinema(String cinemaUrl)
    {

        try {
            // Connessione al sito e download del documento HTML
            Document doc = Jsoup.connect(cinemaUrl).get();

            // Estrazione del nome del cinema (contenuto nel tag h1 con classe "h1")
            String cinema = doc.select("h1.h1").text();

            // Visualizzazione del nome del cinema
            System.out.println("Nome del cinema: " + cinema);

            // Estrai l'indirizzo (completo)
            Element addressElement = doc.select("p[itemprop='address']").first();
            String streetAddress = addressElement.select("span[itemprop='streetAddress']").text();
            String locality = addressElement.select("span[itemprop='addressLocality']").text();
            String region = addressElement.select("span[itemprop='addressRegion']").text();
            System.out.println("Indirizzo: " + streetAddress + ", " + locality + " (" + region + ")");

            // Estrai il numero di telefono
            String telephone = addressElement.select("span[itemprop='telephone']").text();
            System.out.println("Telefono: " + telephone);

            String sql = "INSERT INTO cinema (nome, indirizzo, città, telefono) VALUES (?, ?, ?, ?)";
            try
            {
                DB_Manager.update(sql, cinema, streetAddress, locality, telephone);

            }
            catch (SQLException e)
            {
                System.out.println("Errore di inserimento del cinema: " + cinema);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;

//CLASSE PER OTTENERE LE INFO DEI CINEMA (SCRAPER DA COMING SOON)
public class CS_Scraper
{
    static String baseUrl = "https://www.comingsoon.it";

    /**
     * Metodo per ottenere l'elenco dei cinema (di Vicenza)
     */
    public static void Cinema_scraping()
    {
        try
        {
            String elimina = "DELETE FROM proiettare"; //resetta i dati delle proiezioni
            DB_Manager.update(elimina);
        }
        catch (SQLException e)
        {
            System.out.println("Errore nel ripristino della tabella proiettare");
        }

        try
        {
            Document doc = Jsoup.connect("https://www.comingsoon.it/cinema/vicenza/").get();
            Elements cinemaLinks = doc.select("a.cs-btn.secondary.tag"); // Seleziona i link con questa classe

            for (Element link : cinemaLinks)
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    System.out.println("Errore nell'attesa per il cinema");
                }
                String cinemaUrl = baseUrl + link.attr("href"); //ottieni il link del cinema

                Scraper_DatiCinema(cinemaUrl); //prende i dati del cinema
                Scraper_cinema_singolo(cinemaUrl); //prende i dati sui film proiettati dal cinema
            }
        }
        catch (IOException e)
        {
            System.out.println("Errore nello scraping del cinema!");
        }
    }

    /**
     * Metodo per ottenere le proiezioni del cinema
     * @param cinemaUrl URL Coming Soon del cinema
     */
    public static void Scraper_cinema_singolo(String cinemaUrl)
    {
        try
        {
            Document doc = Jsoup.connect(cinemaUrl).get();

            Elements films = doc.select(".row.rel"); // Selettore per ogni contenitore di film

            for (Element film : films)
            {
                String title = film.select(".tit_olo").text();
                String cinema = doc.select("h1.h1").text();

                try
                {
                    String sql_cinema = "SELECT id_cinema FROM cinema WHERE nome = ?";
                    String sql_film = "SELECT id_film FROM film WHERE titolo = ?";
                    Integer rs_cinema = DB_Manager.query_ID(sql_cinema, cinema);
                    Integer rs_film = DB_Manager.query_ID(sql_film, title);
                    if (rs_film == null)
                        TMDB_Scraper.Ricerca(title, true);
                    rs_film = DB_Manager.query_ID(sql_film, title);
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
            System.out.println("Errore nello scraping del cinema!");
        }
    }

    /**
     * Metodo per ottenere le info del cinema
     * @param cinemaUrl URL Coming Soon del cinema
     */
    public static void Scraper_DatiCinema(String cinemaUrl)
    {

        try
        {
            Document doc = Jsoup.connect(cinemaUrl).get();
            String cinema = doc.select("h1.h1").text();
            System.out.println("Nome del cinema: " + cinema);

            Element addressElement = doc.select("p[itemprop='address']").first();
            String streetAddress = addressElement.select("span[itemprop='streetAddress']").text();
            String locality = addressElement.select("span[itemprop='addressLocality']").text();
            String region = addressElement.select("span[itemprop='addressRegion']").text();
            System.out.println("Indirizzo: " + streetAddress + ", " + locality + " (" + region + ")");

            String telephone = addressElement.select("span[itemprop='telephone']").text();
            System.out.println("Telefono: " + telephone);

            String sql = "SELECT id_cinema FROM cinema WHERE nome = ?";
            try
            {
                Integer id_cinema = DB_Manager.query_ID(sql, cinema);
                if (id_cinema == null)
                {
                    sql = "INSERT INTO cinema (nome, indirizzo, città, telefono) VALUES (?, ?, ?, ?)";
                    DB_Manager.update(sql, cinema, streetAddress, locality, telephone);
                }
            }
            catch (SQLException e)
            {
                System.out.println("Errore di inserimento del cinema: " + cinema);
            }

        }
        catch (IOException e)
        {
            System.out.println("Errore nello scraping del cinema!");
        }
    }
}

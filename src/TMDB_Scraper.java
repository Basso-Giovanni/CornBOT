import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TMDB_Scraper
{
    public static void scraper(String URL)
    {
        try
        {
            Document doc = Jsoup.connect(URL).get();
            String title = doc.select("h2 a").first().text();
            String year = doc.select(".release_date").text().replaceAll("[^0-9]", "");
            String plot = doc.select(".overview p").text();
            String genre = doc.select(".genres a").text();
            String sql = "INSERT INTO Film (titolo, anno_produzione, genere, trama, durata) VALUES (?, ?, ?, ?, 0)";
            DB_Manager.update(sql, title, year, genre, plot);
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nell'estrazione dei dati. URL: " + URL);
        }

    }
}

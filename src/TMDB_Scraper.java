import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TMDB_Scraper
{
    public static void Film_scraper(String URL)
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

            Elements platforms = doc.select(".ott_provider a img");
            StringBuilder platformsList = new StringBuilder();
            for (Element platform : platforms)
                platformsList.append(platform.attr("alt")).append(", ");

            String regista = doc.select("li.profile.director a").text();

            String sql = "INSERT INTO Film (titolo, anno_produzione, genere, trama, durata, data_uscita, piattaforme) VALUES (?, ?, ?, ?, ?, ?, ?)";
            DB_Manager.update(sql, titolo, anno, genere, trama, totalMinutes, formattedDate, platformsList.toString());
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nell'estrazione dei dati. URL: " + URL);
        }

    }
}

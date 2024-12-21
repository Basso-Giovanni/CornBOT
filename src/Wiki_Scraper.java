import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Wiki_Scraper
{
    final static String URL = "https://it.wikipedia.org/wiki/";

    public static void Riconoscimenti_Scraper(String titolo_film)
    {
        String titolo_film_url = titolo_film.replace(" ", "_");
        try {
            // Connettersi al sito
            Document doc = Jsoup.connect(URL + titolo_film_url).get();

            // Set per eliminare duplicati e preservare l'ordine
            Set<String> awards = new LinkedHashSet<>();

            // Seleziona le sezioni di premi (tutti i <ul> e <li>)
            Elements awardSections = doc.select("div.responsive-columns > div > ul > li");

            for (Element awardSection : awardSections) {
                if (awardSection.text().contains(" vm ")) continue;
                // Nome del premio principale (esempio: Premio Oscar)
                String awardName = awardSection.select("> a").text(); // Il link principale del premio (es. "Premio Oscar")
                String awardYear = awardSection.select("> a").first().text().split(" ")[0]; // Estrazione anno (es. "1976")

                // Estrazione delle categorie relative
                Elements categories = awardSection.select("ul > li");

                // Itera sulle categorie
                for (Element category : categories) {
                    String categoryText = category.text();

                    // Ignora candidature
                    if (categoryText.startsWith("Candidatura")) continue;

                    // Costruisci la stringa per singolo riconoscimento
                    String result = awardYear + "_" + awardName + " " + categoryText;

                    // Aggiungi al set per evitare duplicati
                    awards.add(result);
                }
            }

            // Stampa i risultati (una riga per ogni riconoscimento)
            for (String award : awards) {
                System.out.println(award);
            }
        }
        catch (IOException e )
        {
            System.out.println("⚠️ Errore nello scraping dei riconoscimenti da Wikipedia: " + titolo_film);
        }
    }
}

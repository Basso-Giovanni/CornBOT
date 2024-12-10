import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

public class TMDB_API
{
    static final String API_KEY = "8316b32b99cf749bffb0f7ba0cff4191"; //DA TOGLIERE DA QUI

    public static JSONObject GET(int id)
    {
        String apiUrl = "https://api.themoviedb.org/3/movie/" + id + "/watch/providers?api_key=" + API_KEY;

        try
        {
            // Effettua la richiesta HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leggi la risposta
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                response.append(line);
            reader.close();

            // Parsifica la risposta JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject results = jsonResponse.getJSONObject("results");
            return results;
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nella richiesta GET dell'API TMDb");
        }
        return null;
    }
}

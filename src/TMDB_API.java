import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.*;

public class TMDB_API
{
    static final String API_KEY = Config.getTMDBToken();

    public static ArrayList<String> GET_piattaforme(int id)
    {
        ArrayList<String> providers = new ArrayList<>();
        String apiUrl = "https://api.themoviedb.org/3/movie/" + id + "/watch/providers?api_key=" + API_KEY;
        JSONObject film = GET(apiUrl);

        if (film.has("IT"))
        {
            JSONObject provider_italia = film.getJSONObject("IT");

            if (provider_italia.has("flatrate"))
            {
                JSONArray piattaforme = provider_italia.getJSONArray("flatrate");
                for (int i = 0; i < piattaforme.length(); i++)
                {
                    JSONObject piattaforma = piattaforme.getJSONObject(i);
                    providers.add(piattaforma.getString("provider_name"));
                }
            }
        }
        return providers;
    }

    public static String GET_trailer(int id)
    {
        JSONObject film = GET("https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + API_KEY);
        String URL_trailer = null;

        for (int i = 0; i < film.length(); i++)
        {
            JSONObject video = film.getJSONObject("i"); //esempio inutile giusto per togliere l'errore in compilazione
            if (video.getString("type").equalsIgnoreCase("Trailer") &&
                    video.getString("site").equalsIgnoreCase("YouTube") &&
                    video.getBoolean("official")) {

                // Costruisci il link del trailer
                String videoKey = video.getString("key");
                URL_trailer = "https://www.youtube.com/watch?v=" + videoKey;
                break;
            }
        }
        return URL_trailer;
    }

    private static JSONObject GET(String apiUrl)
    {
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.*;

public class TMDB_API
{
    static final String API_KEY = Config.getTMDBToken();

    public static ArrayList<String> GET_piattaforme(int id)
    {
        ArrayList<String> providers = new ArrayList<>();
        String apiUrl = "https://api.themoviedb.org/3/movie/" + id + "/watch/providers?api_key=" + API_KEY;
        JSONObject film = GET(apiUrl).getJSONObject("results");

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
        JSONArray film = GET("https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + API_KEY).getJSONArray("results");
        String URL_trailer = null;

        for (int i = 0; i < film.length(); i++)
        {
            JSONObject video = film.getJSONObject(i);
            if (video.getString("type").equalsIgnoreCase("Trailer") &&
                    video.getString("site").equalsIgnoreCase("YouTube") &&
                    video.getBoolean("official"))
            {
                // Costruisci il link del trailer
                String videoKey = video.getString("key");
                URL_trailer = "https://www.youtube.com/watch?v=" + videoKey;
                break;
            }
        }

        if (URL_trailer == null)
        {
            for (int i = 0; i < film.length(); i++)
            {
                JSONObject video = film.getJSONObject(i);
                if (video.getString("type").equalsIgnoreCase("Trailer") &&
                        video.getString("site").equalsIgnoreCase("YouTube"))
                {
                    // Costruisci il link del trailer
                    String videoKey = video.getString("key");
                    URL_trailer = "https://www.youtube.com/watch?v=" + videoKey;
                    break;
                }
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
            return jsonResponse;
        }
        catch (Exception e)
        {
            System.out.println("⚠️ Errore nella richiesta GET dell'API TMDb");
        }
        return null;
    }

    public static Integer GET_registaIDDaFilm(int id, String nome)
    {
        String apiUrl = "https://api.themoviedb.org/3/movie/" + id + "/credits?api_key=" + API_KEY;
        JSONArray casting = GET(apiUrl).getJSONArray("crew");

        for (int i = 0; i < casting.length(); i++)
        {
            JSONObject cast = casting.getJSONObject(i);
            if (cast.getString("job").equals("Director") && cast.getString("name").equals(nome))
                return cast.getInt("id");
        }
        return null;
    }

    public static HashMap<Integer, String> GET_attoreIDDaFilm(int id)
    {
        HashMap<Integer, String> ids = new HashMap<>();
        String apiUrl = "https://api.themoviedb.org/3/movie/" + id + "/credits?api_key=" + API_KEY;
        JSONArray casting = GET(apiUrl).getJSONArray("cast");

        for (int i = 0; i < casting.length(); i++)
            ids.put(casting.getJSONObject(i).getInt("id"), casting.getJSONObject(i).getString("character"));
        return ids;
    }

    public static ArrayList<String> GET_soggetto(int id)
    {
        JSONObject dati = GET("https://api.themoviedb.org/3/person/" + id + "?api_key=" + API_KEY);
        ArrayList<String> info = new ArrayList<>();
        if (!dati.isNull("name"))
        {
            info.add(dati.getString("name"));
            info.add(dati.isNull("birthday") ? null : dati.getString("birthday"));
            info.add(dati.isNull("deathday") ? null : dati.getString("deathday"));
            info.add(dati.isNull("place_of_birth") ? null : dati.getString("place_of_birth"));
            info.add(dati.isNull("biography") ? null : dati.getString("biography"));
            info.add(dati.isNull("gender") ? null : dati.getInt("gender") == 1 ? "femmina" : "maschio");
        }
        else return null;

        return info;
    }

}

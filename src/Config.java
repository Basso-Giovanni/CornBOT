import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** Classe per la lettura del token del bot Telegram
 *
 */
public class Config
{
    /** Metodo statico per ottenere il token
     *
     * @return token del bot Telegram
     */
    public static String getBotToken()
    {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties"))
        {
            properties.load(input);
        }
        catch (IOException e)
        {
            System.out.println("⚠️ IOExcpetion -> Config.java -> getBotToken() -> Errore nella lettura del file 'config.properties'");
        }
        return properties.getProperty("telegram.bot.token");
    }

    /** Metodo statico per ottenere il token
     *
     * @return token del TMDB API
     */
    public static String getTMDBToken()
    {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties"))
        {
            properties.load(input);
        }
        catch (IOException e)
        {
            System.out.println("⚠️ IOExcpetion -> Config.java -> getTMDBToken() -> Errore nella lettura del file 'config.properties'");
        }
        return properties.getProperty("tmdb.token");
    }
}

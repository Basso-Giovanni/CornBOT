import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    public static void main(String[] args)
    {
//        try
//        {
//            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            botsApi.registerBot(new CornBOT());
//            System.out.println("Bot avviato!");
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }

        TMDB_API.GET_trailer(550);

        //TMDB_Scraper.Film_scraper("https://www.themoviedb.org/movie/680-pulp-fiction", 680);
    }
}
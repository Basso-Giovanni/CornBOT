import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    public static void main(String[] args)
    {
        //TMDB_Scraper.Film_scraper("https://www.themoviedb.org/movie/974576-conclave", 974576);
        try
        {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CornBOT());
            System.out.println("Bot avviato!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //Wiki_Scraper.Riconoscimenti_Scraper("lo squalo (film)");

        //CS_Scraper.Cinema_scraping();
    }
}
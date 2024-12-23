import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    public static void main(String[] args)
    {
        CS_Scraper.Cinema_scraping(); //primo scraping dei cinema e dei film disponibili

        try //avvio del bot telegram
        {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new CornBOT());
            System.out.println("Bot avviato!");
        }
        catch (Exception e)
        {
            System.out.println("Errore nell'avvio del BOT!");
        }
    }
}
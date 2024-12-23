import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main
{
    public static void main(String[] args)
    {
        Thread scrapingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                CS_Scraper.Cinema_scraping();  // Esegui il primo scraping
            }
        });

        //avvio del thread per il cinema scraping
        scrapingThread.start();

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
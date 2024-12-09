import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.ResultSet;

/** Classe per la gestione del bot
 *
 */
public class CornBOT extends TelegramLongPollingBot
{
    final String botUsername = "corntelegrambot";
    final String token = Config.getBotToken();

    /** Getter per username bot
     *
     * @return username bot
     */
    @Override
    public String getBotUsername()
    {
        return botUsername;
    }

    /** Getter per il token del bot
     *
     * @return token del bot
     */
    @Override
    public String getBotToken()
    {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        if (update.hasMessage() && update.getMessage().hasText())
        {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/cercafilm"))
            {
                String titolo = messageText.replace("/cercafilm", "").trim();
                cercaFilm(chatId, titolo);
            }
            else if (messageText.equals("/start"))
            {
                sendMessage(chatId, "Benvenuto su CornBOT 🎞️! Usa /help per vedere i comandi.");
            }
            else
            {
                sendMessage(chatId, "Comando non riconosciuto 🤔. Usa /help per vedere i comandi disponbili.");
            }
        }
    }

    private void cercaFilm(Long chatId, String titolo)
    {
        String sql = "SELECT * FROM Film WHERE titolo LIKE ?";
        try
        {
            //ResultSet rs = DB_Manager.query(sql, "%" + titolo)
        }
    }
}

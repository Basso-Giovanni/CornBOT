import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.sql.SQLException;

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

            if (messageText.startsWith("/cercafilm")) //comando per cercare il film
            {
                String titolo = messageText.replace("/cercafilm", "").trim();
                cercaFilm(chatId, titolo);
            }
            else if (messageText.equals("/start")) //comando per iniziare
                sendMessage(chatId, "Benvenuto su CornBOT 🎞️! Usa /help per vedere i comandi.");
            else if (messageText.startsWith("/cerca"))
            {
                String persona = messageText.replace("/cerca", "").trim();
                cercaPersona(chatId, persona);
            }
            else
                sendMessage(chatId, "Comando non riconosciuto 🤔. Usa /help per vedere i comandi disponbili.");
        }
    }

    private void cercaFilm(Long chatId, String titolo)
    {
        String sql = "SELECT * FROM Film LEFT JOIN soggetto ON Soggetto.id_soggetto = Film.regista WHERE titolo LIKE ?";
        try
        {
            ResultSet rs = DB_Manager.query(sql, "%" + titolo + "%");
            if (rs.next())
            {
                String reply = "Titolo 🎞️: " + rs.getString("titolo") +
                        "\nAnno 📅: " + rs.getInt("anno_produzione") +
                        "\nGenere 👺: " + rs.getString("genere") +
                        "\nDurata 🕑: " + rs.getInt("durata") +
                        "\nRegista 📹: " + rs.getInt("regista") +
                        "\nPiattaforme 📺: " + rs.getString("piattaforme") +
                        "\nTrailer 📺: " + rs.getString("trailer_url");
                sendMessage(chatId, reply);
            }
            else
            {
                sendMessage(chatId, "Film non trovato 😣");
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nella ricerca del film!");
        }
    }

    private void cercaPersona(Long chatId, String nome)
    {
        String sql = "SELECT * FROM Soggetto WHERE nome LIKE ?";
        try
        {
            ResultSet rs = DB_Manager.query(sql, "%" + nome + "%");
            if (rs.next())
            {
                String reply = "Nome  👨‍🎤️: " + rs.getString("titolo") +
                        "\nData di nascita 🎂: " + rs.getInt("anno_produzione") +
                        "\nLuogo di nascita 🗺️: " + rs.getString("genere");
                if (rs.getDate("data_morte") != null )
                    reply += "\nData di morte ⚰️: " + rs.getDate("data_morte").toLocalDate();
                sendMessage(chatId, reply);
            }
            else
                sendMessage(chatId, "Persona non trovata 😣");
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nella ricerca della persona: " + nome);
        }
    }

    private void sendMessage(Long chatId, String text)
    {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try
        {
            execute(message);
        }
        catch (TelegramApiException e)
        {
            System.out.println("⚠️ Errore nell'invio del messaggio!");
        }
    }
}

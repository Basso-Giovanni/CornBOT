import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            {
                sendMessage(chatId, "Benvenuto su CornBOT 🎞️! Usa /help per vedere i comandi.");
                try
                {
                    String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
                    Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
                    if (id_utente == null)
                    {
                        sql = "INSERT INTO utente (telegram_id, nome, cognome, email, calendario_url) VALUES (?, ?, ?, ?, ?)";
                        DB_Manager.update(sql, chatId.intValue(), null, null, null, null);
                    }
                }
                catch (SQLException e)
                {
                    System.out.println("Errore nella creazione dell'utente");
                }
            }
            else if (messageText.startsWith("/cerca"))
            {
                String persona = messageText.replace("/cerca", "").trim();
                cercaPersona(chatId, persona);
            }
            else
                sendMessage(chatId, "Comando non riconosciuto 🤔. Usa /help per vedere i comandi disponbili.");
        }
        if (update.hasCallbackQuery())
        {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (update.getCallbackQuery().getData().contains("biografia_"))
            {
                try
                {
                    String nome = update.getCallbackQuery().getData().split("_")[1];
                    String query = "SELECT biografia FROM Soggetto WHERE nome LIKE ?";
                    ResultSet rs = DB_Manager.query(query, "%" + nome + "%");

                    if (rs.next())
                    {
                        String reply = "BIOGRAFIA DI " + nome.toUpperCase(Locale.ROOT) + " 📖\n" +
                            rs.getString("biografia");
                        sendMessage(chatId, reply);
                    }
                }
                catch (SQLException e)
                {
                    System.out.println("⚠️ Errore nella richiesta della biografia di " + update.getCallbackQuery().getData().split("_")[1]);
                }
            }
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            ResultSet rs = DB_Manager.query(sql, "%" + nome + "%");
            if (rs.next())
            {
                String reply = "Nome  🎭️: " + rs.getString("nome") +
                        "\nData di nascita 🎂: " + rs.getDate("data_nascita").toLocalDate().format(formatter) +
                        "\nLuogo di nascita 🗺️: " + rs.getString("luogo_nascita");
                if (rs.getDate("data_morte") != null )
                    reply += "\nData di morte ⚰️: " + rs.getDate("data_morte").toLocalDate().format(formatter);

                InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

                List<InlineKeyboardButton> lista_btn = new ArrayList<>();
                InlineKeyboardButton btn = new InlineKeyboardButton();
                    btn.setText("Biografia");
                    btn.setCallbackData("biografia_" + rs.getString("nome"));
                lista_btn.add(btn);
                List<List<InlineKeyboardButton>> riga = new ArrayList<>();
                riga.add(lista_btn);
                ikm.setKeyboard(riga);
                sendMessage(chatId, reply, ikm);
            }
            else
                sendMessage(chatId, "Persona non trovata 😣");
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nella ricerca della persona: " + nome);
        }
    }

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup ikm)
    {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(ikm);
        try
        {
            execute(message);
        }
        catch (TelegramApiException e)
        {
            System.out.println("⚠️ Errore nell'invio del messaggio!");
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

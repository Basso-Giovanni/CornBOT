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
            else if (messageText.equals("/watchlist")) //comando per vedere la watchlist
            {
                watchlist(chatId);
            }
            else if (messageText.equals("/preferiti")) //comando per vedere la watchlist
            {
                preferitoFilm(chatId);
            }
            else if (messageText.startsWith("/visto")) //comando per togliere dalla watchlist
            {
                String titolo = messageText.replace("/visto", "").trim();
                remWatchlist(chatId, titolo);
            }
            else if (messageText.startsWith("/aggiungiwatchlist")) //comando per aggiungere alla watchlist
            {
                String titolo = messageText.replace("/aggiungiwatchlist", "").trim();
                addWatchlist(chatId, titolo);
            }
            else if (messageText.equals("/start")) //comando per iniziare
            {
                sendMessage(chatId, "Benvenuto su CornBOT 🎞️! Usa /help per vedere i comandi.");
                try
                {
                    String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
                    Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
                    if (id_utente == null)
                       addUser(chatId);
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
            else if (update.getCallbackQuery().getData().contains("addwatchlist_"))
            {
                String titolo = update.getCallbackQuery().getData().split("_")[1];
                addWatchlist(chatId, titolo);
            }
            else if (update.getCallbackQuery().getData().contains("recensione_"))
            {
//                String titolo = update.getCallbackQuery().getData().split("_")[1];
//                addWatchlist(chatId, titolo);
                //da finire qua, farò domani, non ho voglia adesso
            }
            else if (update.getCallbackQuery().getData().contains("preferito_"))
            {
                Integer id_film = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                addPreferitoFilm(chatId, id_film);
            }
        }
    }

    private void cercaFilm(Long chatId, String titolo)
    {
        String sql = "SELECT *, nome FROM Film LEFT JOIN soggetto ON Soggetto.id_soggetto = Film.regista WHERE titolo LIKE ?";
        try
        {
            ResultSet rs = DB_Manager.query(sql, "%" + titolo + "%");
            if (rs.next())
            {
                String reply = "Titolo 🎞️: " + rs.getString("titolo") +
                        "\nAnno 📅: " + rs.getInt("anno_produzione") +
                        "\nGenere 👺: " + rs.getString("genere") +
                        "\nDurata 🕑: " + rs.getInt("durata") + " minuti" +
                        "\nRegista 📹: " + rs.getString("nome") +
                        "\nPiattaforme 📺: " + rs.getString("piattaforme") + //da sistemare nel caso in cui non ci siano piattaforme
                        "\nTrailer 📺: " + rs.getString("trailer_url");

                InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

                List<InlineKeyboardButton> lista_btn_1 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_2 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_3 = new ArrayList<>();
                InlineKeyboardButton btn_watch = new InlineKeyboardButton();
                InlineKeyboardButton btn_rec = new InlineKeyboardButton();
                InlineKeyboardButton btn_pre = new InlineKeyboardButton();
                btn_watch.setText("Aggiungi alla watchlist 📺");
                btn_rec.setText("Lascia una recensione ⭐");
                btn_pre.setText("Aggiungi ai preferiti ✨");
                btn_watch.setCallbackData("addwatchlist_" + rs.getString("titolo"));
                btn_rec.setCallbackData("recensione_" + rs.getString("titolo"));
                btn_pre.setCallbackData("preferito_" + rs.getInt("id_film"));
                lista_btn_1.add(btn_watch);
                lista_btn_2.add(btn_rec);
                lista_btn_3.add(btn_pre);
                List<List<InlineKeyboardButton>> riga = new ArrayList<>();
                riga.add(lista_btn_1);
                riga.add(lista_btn_2);
                riga.add(lista_btn_3);
                ikm.setKeyboard(riga);
                sendMessage(chatId, reply, ikm);

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

    private void addWatchlist(Long chatId, String titolo_film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            sql = "SELECT id_film FROM film WHERE titolo = ?";
            Integer id_film = DB_Manager.query_ID(sql, titolo_film);

            if (id_utente != null)
            {
                if (id_film != null)
                {
                    try
                    {
                        sql = "INSERT INTO watchlist (utente, film) VALUES (?, ?)";
                        DB_Manager.update(sql, id_utente, id_film);
                        sendMessage(chatId, "Film aggiunto alla tua watchlist!");
                    }
                    catch (SQLException e)
                    {
                        sendMessage(chatId, "Il film " + titolo_film + " è già inserito nella tua watchlist!");
                    }
                }
                else
                    sendMessage(chatId, "Si è verificato un problema nell'inserimento del film " + titolo_film + " 😣");
            }
            else
                addUser(chatId);
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Si è verificato un problema. Riprova più tardi 😣");
        }
    }

    private void watchlist(Long chatId)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            if (id_utente != null)
            {
                sql = "SELECT id_film, titolo FROM watchlist " +
                        "INNER JOIN utente ON utente.id_utente = watchlist.utente " +
                        "INNER JOIN film ON film.id_film = watchlist.film " +
                        "WHERE id_utente = ?";

                ResultSet rs = DB_Manager.query(sql, id_utente);
                ArrayList<String[]> films = new ArrayList<>();
                while (rs.next())
                {
                    Integer id_film = Integer.valueOf(rs.getInt("id_film"));
                    String titolo_film = rs.getString("titolo");
                    films.add(new String[]{id_film.toString(), titolo_film});
                }

                if (films.isEmpty())
                    sendMessage(chatId, "Non hai film nella tua watchlist. Prova a cercare dei titoli o usa il comando /aggiungiwatchlist titolo del film per aggiungerlo direttamente.");

                StringBuilder sb = new StringBuilder();
                sb.append("WATCHLIST 📺\n");
                for (int i = 0; i < films.size(); i++)
                    sb.append(i + 1).append(". ").append(films.get(i)[1]);
                sendMessage(chatId, sb.toString());
            }
            else
            {
                sendMessage(chatId, "Nessun film nella watchlist!");
                addUser(chatId);
            }

        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nell'apertura della watchlist!");
        }
    }

    private void remWatchlist(Long chatId, String titolo)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());

            if (id_utente != null)
            {
                sql = "SELECT id_film, titolo FROM watchlist " +
                        "INNER JOIN utente ON utente.id_utente = watchlist.utente " +
                        "INNER JOIN film ON film.id_film = watchlist.film " +
                        "WHERE id_utente = ? AND titolo LIKE ?";
                ResultSet rs = DB_Manager.query(sql, id_utente, "%" + titolo + "%");

                if (rs.next())
                {
                    sql = "DELETE FROM watchlist WHERE utente = ? AND film = ?";
                    DB_Manager.update(sql, id_utente, rs.getInt("id_film"));
                    sendMessage(chatId, "Rimosso il film " + rs.getString("titolo") + " dalla tua watchlist!");
                }
            }
            else
            {
                sendMessage(chatId, "Film non trovato nella tua watchlist! Digita il comando /watchlist per vedere la tua watchlist");
                addUser(chatId);
            }

        }
        catch (SQLException e)
        {
            System.out.println("Errore nella rimozione del film");
        }
    }

    private void addPreferitoFilm(Long chatId, Integer id_film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());

            if (id_utente != null)
            {
                sql = "INSERT INTO preferiti_film (utente, film) VALUES (?, ?)";
                DB_Manager.update(sql, id_utente, id_film);
                sendMessage(chatId, "Film aggiunto ai preferiti");
            }
            else
            {
                sendMessage(chatId, "Impossibile aggiungere il film ai preferiti 😣. Riprova in un secondo momento.");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nell'inserimento del film tra i preferiti.");
        }
    }

    private void preferitoFilm(Long chatId)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            if (id_utente != null)
            {
                sql = "SELECT id_film, titolo FROM preferiti_film " +
                        "INNER JOIN utente ON utente.id_utente = preferiti_film.utente " +
                        "INNER JOIN film ON film.id_film = preferiti_film.film " +
                        "WHERE id_utente = ?";

                ResultSet rs = DB_Manager.query(sql, id_utente);
                ArrayList<String[]> films = new ArrayList<>();
                while (rs.next())
                {
                    Integer id_film = Integer.valueOf(rs.getInt("id_film"));
                    String titolo_film = rs.getString("titolo");
                    films.add(new String[]{id_film.toString(), titolo_film});
                }

                if (films.isEmpty())
                    sendMessage(chatId, "Non hai film preferiti. Prova a cercare dei titoli.");

                StringBuilder sb = new StringBuilder();
                sb.append("FILM PREFERITI ✨\n");
                for (int i = 0; i < films.size(); i++)
                    sb.append(i + 1).append(". ").append(films.get(i)[1]);
                sendMessage(chatId, sb.toString());
            }
            else
            {
                sendMessage(chatId, "Nessun film preferito!");
                addUser(chatId);
            }

        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella lettura dei film preferiti!");
        }
    }

    private void addUser(Long chatId)
    {
        try
        {
            String sql = "INSERT INTO utente (telegram_id, nome, cognome, email, calendario_url) VALUES (?, ?, ?, ?, ?)";
            DB_Manager.update(sql, chatId.intValue(), null, null, null, null);
        }
        catch (SQLException e)
        {
            System.out.println("Errore nella creazione dell'utente: " + chatId);
        }
    }
}

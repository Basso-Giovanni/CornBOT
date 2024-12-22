import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Classe per la gestione del bot
 *
 */
public class CornBOT extends TelegramLongPollingBot
{
    final String botUsername = "corntelegrambot";
    final String token = Config.getBotToken();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Map<Long, Integer> pendingAppuntiFilm = new HashMap<>();
    private Map<Long, Integer> pendingAppuntiSoggetto = new HashMap<>();
    private Map<Long, Integer> pendingCinema = new HashMap<>();
    private Map<Long, Integer[]> pendingValutazioni = new HashMap<>();

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

            if (pendingAppuntiFilm.containsKey(chatId))
            {
                Integer id_film = pendingAppuntiFilm.remove(chatId);
                addAppunti(chatId, id_film, messageText, true);
            }
            else if (pendingAppuntiSoggetto.containsKey(chatId))
            {
                Integer id_soggetto = pendingAppuntiSoggetto.remove(chatId);
                addAppunti(chatId, id_soggetto, messageText, false);
            }
            else if (pendingValutazioni.containsKey(chatId))
            {
                Integer[] info = pendingValutazioni.remove(chatId);
                upRecensione(chatId, info[0], info[1], messageText);
            }
            else if (pendingCinema.containsKey(chatId))
            {
                Integer id_film = pendingCinema.remove(chatId);
                cinema(chatId, id_film, messageText);
            }
            else if (messageText.startsWith("/cercafilm")) //comando per cercare il film
            {
                String titolo = messageText.replace("/cercafilm", "").trim();
                cercaFilm(chatId, titolo);
            }
            else if (messageText.equals("/watchlist")) //comando per vedere la watchlist
            {
                watchlist(chatId);
            }
            else if (messageText.equals("/preferitifilm")) //comando per vedere i film preferiti
            {
                preferitoFilm(chatId);
            }
            else if (messageText.equals("/preferiti")) //comando per vedere i preferiti
            {
                preferitoSoggetto(chatId);
            }
            else if (messageText.startsWith("/nopreferitifilm")) //comando per rimuovere dai film preferiti
            {
                String titolo = messageText.replace("/nopreferitifilm", "").trim();
                remPreferitoFilm(chatId, titolo);
            }
            else if (messageText.startsWith("/nopreferiti")) //comando per rimuovere dai preferiti
            {
                String nome = messageText.replace("/nopreferiti", "").trim();
                remPreferitoSoggetto(chatId, nome);
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
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                addRecensione(chatId, id);
            }
            else if (update.getCallbackQuery().getData().contains("preferitofilm_"))
            {
                Integer id_film = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                addPreferito(chatId, id_film, true);
            }
            else if (update.getCallbackQuery().getData().contains("preferitosoggetto_"))
            {
                Integer id_soggetto = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                addPreferito(chatId, id_soggetto, false);
            }
            else if (update.getCallbackQuery().getData().contains("appuntofilm_"))
            {
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                pendingAppuntiFilm.put(chatId, id);
                sendMessage(chatId, "Scrivi ora il tuo appunto per il film");
            }
            else if (update.getCallbackQuery().getData().contains("appuntosoggetto_"))
            {
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                pendingAppuntiSoggetto.put(chatId, id);
                sendMessage(chatId, "Scrivi ora il tuo commento per il soggetto");
            }
            else if (update.getCallbackQuery().getData().contains("vediappuntisoggetto_"))
            {
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                appunti(chatId, id, false);
            }
            else if (update.getCallbackQuery().getData().contains("vediappuntifilm_"))
            {
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                appunti(chatId, id, true);
            }
            else if (update.getCallbackQuery().getData().contains("eliminaapp_"))
            {
                Integer id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                remAppunti(chatId, id);
            }
            else if (update.getCallbackQuery().getData().contains("rec_"))
            {
                int rating = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                int id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[2]);
                pendingValutazioni.put(chatId, new Integer[]{rating, id});
                sendMessage(chatId, "Scrivi il contenuto della recensione");
            }
            else if (update.getCallbackQuery().getData().contains("vedirecensioni_"))
            {
                int id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                printRecensioni(chatId, id);
            }
            else if (update.getCallbackQuery().getData().contains("norecensioni_"))
            {
                int id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                remRecensione(chatId, id);
            }
            else if (update.getCallbackQuery().getData().contains("cinema_"))
            {
                int id = Integer.valueOf(update.getCallbackQuery().getData().split("_")[1]);
                pendingCinema.put(chatId, id);
                sendMessage(chatId, "Scrivi la città per cui fare la ricerca del cinema");
            }
        }
    }

    private void cercaFilm(Long chatId, String titolo)
    {
        String sql = "SELECT *, nome FROM Film LEFT JOIN soggetto ON Soggetto.id_soggetto = Film.regista WHERE titolo LIKE ?";
        try
        {
            ResultSet rs = DB_Manager.query(sql, "%" + titolo + "%");
            ResultSet rs_rec;
            if (rs.next())
            {
                String reply = "Titolo 🎞️: " + rs.getString("titolo") +
                        "\nAnno 📅: " + rs.getInt("anno_produzione") +
                        "\nGenere 👺: " + rs.getString("genere") +
                        "\nDurata 🕑: " + rs.getInt("durata") + " minuti" +
                        "\nRegista 📹: " + rs.getString("nome") +
                        "\nPiattaforme 📺: " + rs.getString("piattaforme") + //da sistemare nel caso in cui non ci siano piattaforme
                        "\nTrailer 🍿: " + rs.getString("trailer_url");

                sql = "SELECT * FROM valutazioni WHERE id_film = ?";
                rs_rec = DB_Manager.query(sql, rs.getInt("id_film"));

                if (rs_rec.next())
                    reply += "\nValutazione media ⭐: " + Math.round(Float.valueOf(rs_rec.getString("media")) * 10)/10 + "/4";

                sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
                Integer id = DB_Manager.query_ID(sql, chatId);
                boolean preferito = false;

                if (id != null)
                {
                    sql = "SELECT * FROM preferiti_film WHERE utente = ? AND film = ?";
                    ResultSet pref = DB_Manager.query(sql, id, rs.getInt("id_film"));
                    if (pref.next())
                        preferito = true;
                }

                InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

                List<InlineKeyboardButton> lista_btn_1 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_2 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_3 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_4 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_5 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_6 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_7 = new ArrayList<>();
                InlineKeyboardButton btn_watch = new InlineKeyboardButton();
                InlineKeyboardButton btn_rec = new InlineKeyboardButton();
                InlineKeyboardButton btn_pre = new InlineKeyboardButton();
                InlineKeyboardButton btn_app = new InlineKeyboardButton();
                InlineKeyboardButton btn_vap = new InlineKeyboardButton();
                InlineKeyboardButton btn_vre = new InlineKeyboardButton();
                InlineKeyboardButton btn_nre = new InlineKeyboardButton();
                InlineKeyboardButton btn_cin = new InlineKeyboardButton();
                btn_watch.setText("Aggiungi alla watchlist 📺");
                btn_rec.setText("Lascia una recensione ⭐");
                btn_nre.setText("Elimina recensione ❌️");
                btn_pre.setText("Aggiungi ai preferiti ✨");
                btn_app.setText("Aggiungi un appunto 🖋️");
                btn_vap.setText("Vedi appunti 📁️");
                btn_vre.setText("Vedi recensioni  🧾️");
                btn_cin.setText("️Trova il film nei cinema 📽️");
                btn_watch.setCallbackData("addwatchlist_" + rs.getString("titolo"));
                btn_rec.setCallbackData("recensione_" + rs.getInt("id_film"));
                btn_pre.setCallbackData("preferitofilm_" + rs.getInt("id_film"));
                btn_app.setCallbackData("appuntofilm_" + rs.getInt("id_film"));
                btn_vap.setCallbackData("vediappuntifilm_" + rs.getInt("id_film"));
                btn_vre.setCallbackData("vedirecensioni_" + rs.getInt("id_film"));
                btn_nre.setCallbackData("norecensioni_" + rs.getInt("id_film"));
                btn_cin.setCallbackData("cinema_" + rs.getInt("id_film"));
                lista_btn_1.add(btn_watch);
                sql = "SELECT * FROM recensione INNER JOIN utente on utente.id_utente = recensione.utente WHERE film = ? AND telegram_id = ?";
                rs_rec = DB_Manager.query(sql, rs.getInt("id_film"), chatId);
                if (rs_rec.next())
                    lista_btn_2.add(btn_nre);
                else
                    lista_btn_2.add(btn_rec);
                lista_btn_3.add(btn_pre);
                lista_btn_4.add(btn_app);
                lista_btn_5.add(btn_vap);
                sql = "SELECT * FROM valutazioni WHERE id_film = ?";
                rs_rec = DB_Manager.query(sql, rs.getInt("id_film"));
                if (rs_rec.next())
                    lista_btn_6.add(btn_vre);
                lista_btn_7.add(btn_cin);
                List<List<InlineKeyboardButton>> riga = new ArrayList<>();
                riga.add(lista_btn_1);
                riga.add(lista_btn_2);
                if (!preferito)
                    riga.add(lista_btn_3);
                riga.add(lista_btn_4);
                riga.add(lista_btn_5);
                riga.add(lista_btn_6);
                riga.add(lista_btn_7);
                ikm.setKeyboard(riga);
                sendMessage(chatId, reply, ikm);
            }
            else
                sendMessage(chatId, "Film non trovato 😣");
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
                String reply = "Nome  🎭️: " + rs.getString("nome") +
                        "\nData di nascita 🎂: " + rs.getDate("data_nascita").toLocalDate().format(formatter) +
                        "\nLuogo di nascita 🗺️: " + rs.getString("luogo_nascita");
                if (rs.getDate("data_morte") != null )
                    reply += "\nData di morte ⚰️: " + rs.getDate("data_morte").toLocalDate().format(formatter);

                sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
                Integer id = DB_Manager.query_ID(sql, chatId);
                boolean preferito = false;

                if (id != null)
                {
                    sql = "SELECT * FROM preferiti_soggetti WHERE utente = ? AND soggetto = ?";
                    ResultSet pref = DB_Manager.query(sql, id, rs.getInt("id_soggetto"));
                    if (pref.next())
                        preferito = true;
                }

                InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

                List<InlineKeyboardButton> lista_btn_1 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_2 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_3 = new ArrayList<>();
                List<InlineKeyboardButton> lista_btn_4 = new ArrayList<>();
                InlineKeyboardButton btn_bio = new InlineKeyboardButton();
                    btn_bio.setText("Biografia 📖");
                    btn_bio.setCallbackData("biografia_" + rs.getString("nome"));
                lista_btn_1.add(btn_bio);
                InlineKeyboardButton btn_app = new InlineKeyboardButton();
                    btn_app.setText("Aggiungi un appunto 🖋️");
                    btn_app.setCallbackData("appuntosoggetto_" + rs.getInt("id_soggetto"));
                lista_btn_2.add(btn_app);
                InlineKeyboardButton btn_vap = new InlineKeyboardButton();
                    btn_vap.setText("Vedi appunti 📁");
                    btn_vap.setCallbackData("vediappuntisoggetto_" + rs.getInt("id_soggetto"));
                lista_btn_3.add(btn_vap);
                InlineKeyboardButton btn_pre = new InlineKeyboardButton();
                    btn_pre.setText("Aggiungi ai preferiti ✨");
                    btn_pre.setCallbackData("preferitosoggetto_" + rs.getInt("id_soggetto"));
                lista_btn_4.add(btn_pre);
                List<List<InlineKeyboardButton>> riga = new ArrayList<>();
                riga.add(lista_btn_1);
                riga.add(lista_btn_2);
                riga.add(lista_btn_3);
                if (!preferito)
                    riga.add(lista_btn_4);
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

    private void addPreferito(Long chatId, Integer id, Boolean film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());

            if (id_utente != null)
            {
                if (film)
                    sql = "INSERT INTO preferiti_film (utente, film) VALUES (?, ?)";
                else
                    sql = "INSERT INTO preferiti_soggetti (utente, soggetto) VALUES (?, ?)";
                DB_Manager.update(sql, id_utente, id);
                sendMessage(chatId, "Aggiunto ai preferiti correttamente!");
            }
            else
            {
                sendMessage(chatId, "Impossibile aggiungere ai preferiti 😣. Riprova in un secondo momento.");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nell'inserimento nei preferiti.");
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
                {
                    sendMessage(chatId, "Non hai film preferiti. Prova a cercare dei titoli.");
                    return;
                }

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

    private void preferitoSoggetto(Long chatId)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            if (id_utente != null)
            {
                sql = "SELECT id_soggetto, soggetto.nome FROM preferiti_soggetti " +
                        "INNER JOIN utente ON utente.id_utente = preferiti_soggetti.utente " +
                        "INNER JOIN soggetto ON soggetto.id_soggetto = preferiti_soggetti.soggetto " +
                        "WHERE id_utente = ?";

                ResultSet rs = DB_Manager.query(sql, id_utente);
                ArrayList<String[]> sogg = new ArrayList<>();
                while (rs.next())
                {
                    Integer id_soggetto = Integer.valueOf(rs.getInt("id_soggetto"));
                    String nome = rs.getString("soggetto.nome");
                    sogg.add(new String[]{id_soggetto.toString(), nome});
                }

                if (sogg.isEmpty())
                {
                    sendMessage(chatId, "Non hai attori/registi preferiti. Prova a cercarne con il comando /cerca nome.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("REGISTI/ATTORI PREFERITI ✨\n");
                for (int i = 0; i < sogg.size(); i++)
                    sb.append(i + 1).append(". ").append(sogg.get(i)[1]);
                sendMessage(chatId, sb.toString());
            }
            else
            {
                sendMessage(chatId, "Nessun preferito!");
                addUser(chatId);
            }

        } catch (SQLException e) {
            sendMessage(chatId, "Errore nella lettura dei preferiti!");
        }
    }

    private void remPreferitoFilm(Long chatId, String titolo)
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
                        "WHERE id_utente = ? AND titolo LIKE ?";
                ResultSet rs = DB_Manager.query(sql, id_utente, "%" + titolo + "%");

                if (rs.next())
                {
                    sql = "DELETE FROM preferiti_film WHERE utente = ? AND film = ?";
                    DB_Manager.update(sql, id_utente, rs.getInt("id_film"));
                    sendMessage(chatId, "Rimosso il film " + rs.getString("titolo") + " dai tuoi preferiti!");
                }
            }
            else
            {
                sendMessage(chatId, "Film non trovato tra i tuoi preferiti! Digita il comando /preferitifilm per vedere i tuoi preferiti");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Errore nella rimozione del film");
        }
    }

    private void remPreferitoSoggetto(Long chatId, String nome)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());

            if (id_utente != null)
            {
                sql = "SELECT id_soggetto, soggetto.nome FROM preferiti_soggetti " +
                        "INNER JOIN utente ON utente.id_utente = preferiti_soggetti.utente " +
                        "INNER JOIN soggetto ON soggetto.id_soggetto = preferiti_soggetti.soggetto " +
                        "WHERE id_utente = ? AND soggetto.nome LIKE ?";
                ResultSet rs = DB_Manager.query(sql, id_utente, "%" + nome + "%");

                if (rs.next())
                {
                    sql = "DELETE FROM preferiti_soggetti WHERE utente = ? AND soggetto = ?";
                    DB_Manager.update(sql, id_utente, rs.getInt("id_soggetto"));
                    sendMessage(chatId, "Rimosso " + rs.getString("soggetto.nome") + " dai tuoi preferiti!");
                }
            }
            else
            {
                sendMessage(chatId, "Persona non trovata tra i tuoi preferiti! Digita il comando /preferiti per vedere i tuoi preferiti");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Errore nella rimozione della persona!");
        }
    }

    private void addAppunti(Long chatId, Integer id, String appunti, Boolean film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());

            if (id_utente != null)
            {
                if (film)
                {
                    sql = "INSERT INTO appunti (utente, film, contenuto) VALUES (?, ?, ?)";
                    DB_Manager.update(sql, id_utente, id, appunti);
                    sendMessage(chatId, "Appunto aggiunto al film");
                }
                else
                {
                    sql = "INSERT INTO appunti (utente, soggetto, contenuto) VALUES (?, ?, ?)";
                    DB_Manager.update(sql, id_utente, id, appunti);
                    sendMessage(chatId, "Appunto aggiunto al soggetto");
                }
            }
            else
            {
                sendMessage(chatId, "Impossibile aggiungere l'appunto 😣. Riprova in un secondo momento.");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "⚠️ Errore nell'inserimento dell'appunto.");
        }
    }

    private void appunti(Long chatId, Integer id, Boolean film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            ResultSet rs;

            if (id_utente != null)
            {
                if (film)
                {
                    sql = "SELECT * FROM appunti WHERE utente = ? AND film = ?";
                }
                else
                {
                    sql = "SELECT * FROM appunti WHERE utente = ? AND soggetto = ?";
                }

                rs = DB_Manager.query(sql, id_utente, id);

                if (!rs.isBeforeFirst()) //rs è vuoto
                {
                    sendMessage(chatId, "Nessun appunto trovato. Prova a scrivere degli appunti con il pulsante Aggiungi un appunto 🖋️.");
                    return;
                }

                String reply; int index;
                while (rs.next())
                {
                    index = 1;
                    reply = "APPUNTO #" + index + "\n" +
                            rs.getString("contenuto");

                    InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

                    List<InlineKeyboardButton> lista_btn = new ArrayList<>();
                    InlineKeyboardButton btn_bio = new InlineKeyboardButton();
                    btn_bio.setText("Elimina appunto ❌");
                    btn_bio.setCallbackData("eliminaapp_" + rs.getInt("id_appunto"));
                    lista_btn.add(btn_bio);
                    List<List<InlineKeyboardButton>> riga = new ArrayList<>();
                    riga.add(lista_btn);
                    ikm.setKeyboard(riga);

                    sendMessage(chatId, reply, ikm);
                }
            }
            else
            {
                sendMessage(chatId, "Nessun appunto trovato. Prova a scrivere degli appunti con il pulsante Aggiungi un appunto 🖋️.");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella ricerca degli appunti!");
        }
    }

    private void remAppunti(Long chatId, Integer id)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId.intValue());
            ResultSet rs;

            if (id_utente != null)
            {
                sql = "SELECT id_appunto FROM appunti WHERE id_appunto = ?";
                if (DB_Manager.query_ID(sql, id) != null)
                {
                    sql = "DELETE FROM appunti WHERE id_appunto = ?";
                    DB_Manager.update(sql, id);
                    sendMessage(chatId, "Appunto cancellato correttamente!");
                }
                else
                    sendMessage(chatId, "Impossibile cancellare l'appunto. Probabilmente è già stato cancellato o non è mai stato creato.");
            }
            else
            {
                sendMessage(chatId, "Impossibile eliminare l'appunto selezionato.");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella cancellazione dell'appunto!");
        }
    }

    private void addRecensione(Long chatId, Integer id)
    {
        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> lista_btn_1 = new ArrayList<>();
        List<InlineKeyboardButton> lista_btn_2 = new ArrayList<>();
        InlineKeyboardButton btn_1 = new InlineKeyboardButton();
        InlineKeyboardButton btn_2 = new InlineKeyboardButton();
        InlineKeyboardButton btn_3 = new InlineKeyboardButton();
        InlineKeyboardButton btn_4 = new InlineKeyboardButton();
        btn_1.setText("⭐");
        btn_2.setText("⭐⭐");
        btn_3.setText("⭐⭐⭐");
        btn_4.setText("️⭐⭐⭐⭐");
        btn_1.setCallbackData("rec_1_" + id);
        btn_2.setCallbackData("rec_2_" + id);
        btn_3.setCallbackData("rec_3_" + id);
        btn_4.setCallbackData("rec_4_" + id);
        lista_btn_1.add(btn_1);
        lista_btn_1.add(btn_2);
        lista_btn_2.add(btn_3);
        lista_btn_2.add(btn_4);
        List<List<InlineKeyboardButton>> riga = new ArrayList<>();
        riga.add(lista_btn_1);
        riga.add(lista_btn_2);
        ikm.setKeyboard(riga);
        sendMessage(chatId, "Scegli un voto da 1 a 4 ⭐", ikm);
    }

    private void upRecensione(Long chatId, Integer rating, Integer id_film, String testo)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId);

            if (id_utente != null)
            {
                sql = "INSERT INTO recensione (data, testo, rating, utente, film) VALUES (?, ?, ?, ?, ?)";
                DB_Manager.update(sql, Date.valueOf(LocalDate.now()), testo, rating, id_utente, id_film);
                sendMessage(chatId, "Recensione creata correttamente!");
            }
            else
            {
                sendMessage(chatId, "Errore nella creazione della recensione!");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella creazione della recensione!");
        }
    }

    private void printRecensioni(Long chatId, Integer id_film)
    {
        try
        {
            String sql = "SELECT * FROM recensione INNER JOIN film ON film.id_film = recensione.film WHERE film = ?";
            ResultSet rs = DB_Manager.query(sql, id_film);
            StringBuilder sb = new StringBuilder();
            sb.append("RECENSIONI DEL FILM ").append("⭐").append("\nLe recensioni sono sempre anonime!");

            while (rs.next())
            {
                sb.append("\nVoto: ");
                for (int i = 0; i < rs.getInt("rating"); i++)
                    sb.append("⭐");

                sb.append("\nData: ").append(rs.getDate("data").toLocalDate().format(formatter));

                sb.append("\n").append(rs.getString("testo")).append("\n");
            }

            sendMessage(chatId, sb.toString());
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella stampa delle recensioni!");
        }
    }

    private void remRecensione(Long chatId, Integer id_film)
    {
        try
        {
            String sql = "SELECT id_utente FROM utente WHERE telegram_id = ?";
            Integer id_utente = DB_Manager.query_ID(sql, chatId);

            if (id_utente != null)
            {
                sql = "SELECT id_recensione FROM recensione WHERE utente = ? AND film = ?";
                Integer id_recensione = DB_Manager.query_ID(sql, id_utente, id_film);

                if (id_recensione != null)
                {
                    sql = "DELETE FROM recensione WHERE id_recensione = ?";
                    DB_Manager.update(sql, id_recensione);
                    sendMessage(chatId, "Recensione cancellata con successo!");
                }
                else
                    sendMessage(chatId, "Impossibile eliminare la recensione. Probabilmente è già stata eliminata.");
            }
            else
            {
                sendMessage(chatId, "Recensione da eliminare non trovata!");
                addUser(chatId);
            }
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Impossibile eliminare la recensione!");
        }
    }

    private void cinema(Long chatId, Integer id, String citta)
    {
        try
        {
            String sql = "SELECT * FROM proiettare " +
                    "INNER JOIN film ON film.id_film = proiettare.film " +
                    "INNER JOIN cinema ON cinema.id_cinema = proiettare.cinema " +
                    "WHERE id_film = ? AND cinema.città LIKE ?";
            ResultSet rs = DB_Manager.query(sql, id, "%" + citta + "%");

            if (rs.isBeforeFirst())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("CINEMA CHE RIPRODUCONO IL FILM");
                while(rs.next())
                {
                    sb.append("\n - ").append(rs.getString("cinema.nome")).append(", ")
                            .append(rs.getString("indirizzo"))
                            .append(", ").append("telefono");
                }
                sendMessage(chatId, sb.toString());
            }
            else
                sendMessage(chatId, citta + ": non ci sono cinema con questo film. Prova a cercare un'altra città.");
        }
        catch (SQLException e)
        {
            sendMessage(chatId, "Errore nella ricerca del cinema!");
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

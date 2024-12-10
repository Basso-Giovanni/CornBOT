import java.sql.*;

/** Classe per la gestione del database
 *
 */
public class DB_Manager
{
    static final String URL = "jdbc:mysql://localhost:3306/cornbot";
    static final String USER = "root";
    static final String PASSWORD = "";

    /** Metodo statico per realizzare la connessione con il database
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Metodo statico per eseguire una query
     *
     * @param sql stringa della query da eseguire
     * @param params parametri eventuali
     * @return ResultSet contenenti i risultati della query
     * @throws SQLException
     */
    public static ResultSet query(String sql, Object... params) throws SQLException
    {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
        {
            ps.setObject(i + 1, params[i]);
        }
        return ps.executeQuery();
    }

    /** Metodo statico per eseguire un UPDATE al database
     *
     * @param sql stringa contenente la query da eseguire
     * @param params parametri eventuali
     * @throws SQLException
     */
    public static void update(String sql, Object... params) throws SQLException
    {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
        {
            ps.setObject(i + 1, params[i]);
        }
        ps.executeUpdate();
    }
}

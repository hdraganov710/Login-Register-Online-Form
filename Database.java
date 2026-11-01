import java.sql.*;

public class Database {
    private static final String url = "jdbc:mysql://localhost:3306/telebid";
    private static final String dbUser = "root";
    private static final String dbPassword = System.getenv("DB_PASS");
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }

}

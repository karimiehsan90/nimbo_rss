package in.nimbo.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DAO {
    private static Connection connection;
    private static Properties databaseProp;

    private static void getProperties() {
        try {
            databaseProp = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream is = loader.getResourceAsStream("database.properties");
            databaseProp.load(is);
        } catch (IOException e) {
            throw new RuntimeException("database properties not found", e);
        }
    }

    public static Connection getConnection() {
        getProperties();
        if (connection == null) {
            try {
                String url = databaseProp.getProperty("database.url");
                String username = databaseProp.getProperty("database.username");
                String password = databaseProp.getProperty("database.password");
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                throw new RuntimeException("Unable to connect to MySQL: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("MySQL driver not found");
            }
        }
        return connection;
    }

    public DAO() {

    }
}

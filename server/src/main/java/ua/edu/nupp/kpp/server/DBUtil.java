package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {
    // Параметри підключення беруться зі змінних середовища, щоб пароль не потрапляв у код і GitHub.
    // Якщо змінну не задано, використовується значення за замовчуванням для локального сервера MySQL.
    private static final String URL = env("MUSEUM_DB_URL", "jdbc:mysql://localhost:3306/museum_exhibits");
    private static final String USER = env("MUSEUM_DB_USER", "root");
    private static final String PASSWORD = env("MUSEUM_DB_PASSWORD", "");

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL драйвер не знайдено", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

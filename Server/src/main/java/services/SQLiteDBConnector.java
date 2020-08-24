package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDBConnector implements DBConnector {

    private final Connection connection = null;
    private final String URL = "jdbc:sqlite:./users.db";

    public void start() {
        System.out.println("Сервер базы данных запущен.");
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных!");
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения с базой данных!");
            e.printStackTrace();
        }
    }

}

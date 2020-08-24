package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    private final DBConnector dbConnector;

    public AuthService(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
        resetIsLogin();
    }

    public synchronized String getUserIDByLoginAndPassword(String login, String password) {
        String userID = null;
        try {
            Connection connection = dbConnector.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE login = ? AND password = ?"
            );
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userID = resultSet.getString("id");
            }
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка получения данных из базы!");
            e.printStackTrace();
        }
        return userID;
    }

    public void setIsLogin(String id, boolean isLogin) {
        try {
            Connection connection = dbConnector.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET isLogin = ? WHERE id = ?"
            );
            statement.setInt(1, isLogin ? 1 : 0);
            statement.setString(2, id);
            statement.execute();
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка изменения данных в базе!");
            e.printStackTrace();
        }
    }

    public boolean isLogin(String id) {
        try {
            Connection connection = dbConnector.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT login FROM users WHERE id = ? AND isLogin = 1"
            );
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка получения данных из базы!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIsUsedUserId(String login) {
        try {
            Connection connection = dbConnector.getConnection();

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE login = ?"
            );
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return false;
            }
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка получения данных из базы!");
            e.printStackTrace();
        }
        return true;
    }

    public void registerNewUser(String login, String password) {
        try {
            Connection connection = dbConnector.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (login, password) VALUES (?, ?)"
            );
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка изменения данных в базе!");
            e.printStackTrace();
        }
    }

    private void resetIsLogin() {
        try {
            Connection connection = dbConnector.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET isLogin = 0"
            );
            statement.execute();
            dbConnector.closeConnection(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка изменения данных в базе!");
            e.printStackTrace();
        }
    }
}

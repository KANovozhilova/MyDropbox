package services;

import java.sql.Connection;

public interface DBConnector {

    void start();

    Connection getConnection();

    void closeConnection(Connection connection);

}

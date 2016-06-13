package com.dummyc0m.bungeecord.datacord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Dummyc0m on 3/8/16.
 */
public class DatabaseConnectionFactory {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnectionFactory(String type, String hostname, int port, String database, String username, String password) {
        StringBuilder stringBuilder = (new StringBuilder("jdbc:"))
                .append(type).append("://").append(hostname).append(":").append(port);
        if(database != null) {
            stringBuilder.append("/").append(database);
        }
        url = stringBuilder.toString();
        this.username = username;
        this.password = password;
    }

    public Connection create() {
        try {
            if(username != null && password != null) {
                return DriverManager.getConnection(url, username, password);
            } else {
                return DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void discard(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

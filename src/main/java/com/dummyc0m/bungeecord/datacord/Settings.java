package com.dummyc0m.bungeecord.datacord;

/**
 * Created by Dummyc0m on 3/8/16.
 */
public class Settings {
    private String type;
    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private String duplicateConnectionMessage;
    private String loadFailedMessage;

    private int dataServerPort;

    public Settings() {
        type = "mysql";
        hostname = "localhost";
        port = 3306;
        database = "MyDatabase";
        username = "user";
        password = "password";

        duplicateConnectionMessage = "[DataCord] Connection error, please try again later.";
        loadFailedMessage = "[DataCord] Failed to load your data, please try again";

        dataServerPort = 25555;
    }

    public Settings(String type, String hostname, int port, String database, String username, String password, String duplicateConnectionMessage, String loadFailedMessage, int dataServerPort) {
        this.type = type;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.duplicateConnectionMessage = duplicateConnectionMessage;
        this.loadFailedMessage = loadFailedMessage;
        this.dataServerPort = dataServerPort;
    }

    public String getType() {
        return type;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDuplicateConnectionMessage() {
        return duplicateConnectionMessage;
    }

    public String getLoadFailedMessage() {
        return loadFailedMessage;
    }

    public int getDataServerPort() {
        return dataServerPort;
    }
}

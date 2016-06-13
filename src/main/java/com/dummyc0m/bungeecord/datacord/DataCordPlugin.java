package com.dummyc0m.bungeecord.datacord;

import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;

/**
 * Plugin Channel: DataCord
 *
 * Sent when the client connects to the target server.
 * Command: Connect
 * Argument: uuid, data
 * (DataCord)
 *
 * Send when you try to save the data to DataCord
 * Command: Save
 * Argument: uuid, data
 *
 * Send when the player disconnects from your server
 * Command: Disconnect
 * Argument: uuid, data
 */
public class DataCordPlugin extends Plugin {
    private DataInterface dataInterface;
    private ConfigFile configFile;

    @Override
    public void onEnable() {
        getProxy().registerChannel("DataCord");
        configFile = new ConfigFile(getDataFolder(), "database.json", Settings.class);
        Settings settings = (Settings) configFile.getSettings();
        assert settings != null;
        try {
            dataInterface = new DataInterface(settings.getType(),
                    settings.getHostname(),
                    settings.getPort(),
                    settings.getDatabase(),
                    settings.getUsername(),
                    settings.getPassword(),
                    this,
                    settings.getDuplicateConnectionMessage(),
                    settings.getLoadFailedMessage(),
                    settings.getDataServerPort());
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Unable to connect to database");
        }
        getLogger().info("DataCord Initialized");
    }

    @Override
    public void onDisable() {
        configFile.save();
        dataInterface.terminateSave(true);
    }


}

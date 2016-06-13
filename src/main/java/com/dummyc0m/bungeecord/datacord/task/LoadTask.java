package com.dummyc0m.bungeecord.datacord.task;

import com.dummyc0m.bungeecord.datacord.DataCache;
import com.dummyc0m.bungeecord.datacord.DataInterface;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dummyc0m on 7/7/15.
 * Ideally Functional
 */
public class LoadTask implements Runnable {
    private static final String GET_DATA;
    private static final String INSERT_NEW;
    private static final String SET_ONLINE;

    static {
        GET_DATA = "SELECT `Data`,`OnlineLock`,`Last` FROM `DataCordData` WHERE `Player` = ?";
        INSERT_NEW = "INSERT INTO `DataCordData`(`Player`,`OnlineLock`) VALUES(?,1)";
        SET_ONLINE = "UPDATE `DataCordData` SET `OnlineLock` = 1 WHERE `Player` = ?";
    }

    private final ProxiedPlayer player;
    private final UUID uuid;
    private final DataInterface dataInterface;
    private final Plugin plugin;
    private final ProxyServer proxy;
    private final TextComponent loadFailedMessage;

    public LoadTask(DataInterface dataInterface, UUID uuid, TextComponent loadFailedMessage, ProxiedPlayer player) {
        this.dataInterface = dataInterface;
        this.plugin = dataInterface.getPlugin();
        this.proxy = plugin.getProxy();
        this.uuid = uuid;
        this.loadFailedMessage = loadFailedMessage;
        this.player = player;
    }

    @Override
    public void run() {
        if (!load()) {
            proxy.getScheduler().schedule(plugin, () -> player.disconnect(loadFailedMessage), 0, TimeUnit.MILLISECONDS);
        } else {
            Server server = player.getServer();
            if(server != null) {
                dataInterface.sendConnectData(player);
            }
        }
    }

    private boolean load() {
        try {
            Connection connection = dataInterface.create();
            PreparedStatement getData = connection.prepareStatement(GET_DATA);
            getData.setString(1, uuid.toString());
            ResultSet resultSet = getData.executeQuery();
            if (!resultSet.next()) {
                executeStatement(INSERT_NEW);
                processData(DataCache.EMPTY);
            } else if (resultSet.getInt(2) == 0) {
                executeStatement(SET_ONLINE);
                processData(resultSet.getString(1));
            } else if (resultSet.getLong(3) != 0 && (System.currentTimeMillis() - resultSet.getLong(3)) > 300000) {
                // If locked and not being saved for 5 minutes, then assume player logged off forcibly
                String data = resultSet.getString(1);
                processData(data != null ? data : DataCache.EMPTY);
            } else {
                getData.close();
                dataInterface.discard(connection);
                return false;
            }
            getData.close();
            dataInterface.discard(connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void executeStatement(String statement) throws SQLException {
        Connection connection = dataInterface.create();
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
        dataInterface.discard(connection);
    }

    private void processData(String data) {
        //LOADED STAGE
        dataInterface.getCache().cacheData(uuid, data);
        dataInterface.getCache().getStateMap().put(uuid, DataCache.PlayerState.READY);
    }
}

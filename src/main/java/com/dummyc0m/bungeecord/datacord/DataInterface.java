package com.dummyc0m.bungeecord.datacord;

import com.dummyc0m.bungeecord.datacord.server.DataServer;
import com.dummyc0m.bungeecord.datacord.task.LoadTask;
import com.dummyc0m.bungeecord.datacord.task.SaveTask;
import com.dummyc0m.bungeecord.datacord.task.UnlockTask;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dummyc0m on 5/10/15.
 * Ideally functional
 */
public class DataInterface extends DatabaseConnectionFactory {
    private final DataServer server;
    private final DataCache cache;
    private final ExecutorService service;
    private final Plugin plugin;
    private final TextComponent duplicateConnectionMessage;
    private final TextComponent loadFailedMessage;

    public DataInterface(String type, String hostname, int port, String database,
                         String username, String password, Plugin plugin,
                         String duplicateConnectionMessage, String loadFailedMessage, int dataServerPort) throws SQLException {
        super(type, hostname, port, database, username, password);
        this.plugin = plugin;
        cache = new DataCache();
        server = new DataServer(dataServerPort, cache);
        service = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 60000, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
        this.duplicateConnectionMessage = new TextComponent();
        this.loadFailedMessage = new TextComponent();
        this.duplicateConnectionMessage.setText(duplicateConnectionMessage);
        this.loadFailedMessage.setText(loadFailedMessage);
        plugin.getProxy().getPluginManager().registerListener(plugin, new DataListener(plugin, this, duplicateConnectionMessage));
        server.start();

        Connection connection = create();
        if(connection == null) {
            throw new SQLException("connection is null");
        }
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS DataCordData("
                + "`Id` int NOT NULL AUTO_INCREMENT, "
                + "`Player` char(36) NULL, "
                + "`Data` text NULL, "
                + "`OnlineLock` int NULL, "
                + "`Last` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                + "PRIMARY KEY(`Id`), "
                + "INDEX `player_index` (`Player`));";
        statement.execute(sql);
        statement.close();
        discard(connection);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public DataCache getCache() {
        return cache;
    }

    public void queueLoad(ProxiedPlayer player) {
        if (player == null || !player.isConnected()) {
            throw new IllegalArgumentException("Player is null or disconnected");
        } else if (!player.isConnected()) {
            // Player is gone
            cache.getStateMap().remove(player.getUniqueId());
        } else {
            plugin.getLogger().info("loading");
            service.execute(new LoadTask(this, player.getUniqueId(), loadFailedMessage, player));
        }
    }

    public void queueSave(ProxiedPlayer player, boolean unlock) {
        if (player == null) {
            throw new IllegalArgumentException("Player is null");
        }
        UUID uuid = player.getUniqueId();
        service.execute(new SaveTask(this, uuid, cache.getDataMap().get(player.getUniqueId()), unlock));
        //cache.getDataMap().remove(player.getUniqueId());
    }

    public void queueUnlock(UUID uuid) {
        service.execute(new UnlockTask(this, uuid));
    }

    public void terminateSave(boolean unlock) {
        server.close();
        if(!cache.getDataMap().isEmpty()) {
            service.execute(new SaveTask(this, cache.getDataMap(), unlock));
        }
        try {
            service.awaitTermination(60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendConnectData(ProxiedPlayer player) {
        plugin.getLogger().info("sending connect packet");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(cache.getDataMap().get(player.getUniqueId()));
        player.getServer().sendData("DataCord", out.toByteArray());
        cache.getStateMap().put(player.getUniqueId(), DataCache.PlayerState.CONNECTED);
    }

    public void receivePlayerData(byte[] message) {
        plugin.getLogger().info("receiving player data");
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String command = in.readUTF();
        if ("Save".equals(command)) {
            plugin.getLogger().info("save");
            UUID uuid = UUID.fromString(in.readUTF());
            cache.getDataMap().put(uuid, in.readUTF());
            queueSave(getPlugin().getProxy().getPlayer(uuid), false);
        } else if ("Disconnect".equals(command)) {
            plugin.getLogger().info("disconnect");
            UUID uuid = UUID.fromString(in.readUTF());
            cache.getDataMap().put(uuid, in.readUTF());
            cache.getStateMap().put(uuid, DataCache.PlayerState.READY);
        }
    }
}

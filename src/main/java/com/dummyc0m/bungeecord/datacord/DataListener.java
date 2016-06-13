package com.dummyc0m.bungeecord.datacord;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dummyc0m on 7/7/15.
 * Ideally functional
 */
public class DataListener implements Listener {
    private final DataInterface dataInterface;
    private final DataCache dataCache;
    private final TextComponent disconnectMessage;
    private final Plugin plugin;

    public DataListener(Plugin plugin, DataInterface dataInterface, String disconnectMessage) {
        this.plugin = plugin;
        this.dataInterface = dataInterface;
        dataCache = dataInterface.getCache();
        this.disconnectMessage = new TextComponent();
        this.disconnectMessage.setText(disconnectMessage);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        DataCache.PlayerState playerState = dataCache.getStateMap().get(uuid);
        if(playerState != null && playerState != DataCache.PlayerState.LOADING) {
            plugin.getLogger().info("disconnecting due to already online");
            event.getPlayer().disconnect(disconnectMessage);
        } else {
            dataCache.getStateMap().put(uuid, DataCache.PlayerState.LOADING);
            dataInterface.queueLoad(event.getPlayer());
        }
        //TODO Time Configuration
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        DataCache.PlayerState state = dataCache.getStateMap().get(uuid);
        if(state == DataCache.PlayerState.READY) {
            dataInterface.getCache().getStateMap().remove(uuid);
            dataInterface.queueSave(player, true);
        } else if(state == DataCache.PlayerState.CONNECTED) {
            plugin.getProxy().getScheduler().schedule(plugin, new DelayedSaveTask(0, player), 500, TimeUnit.MILLISECONDS);
        } else {
            dataInterface.getCache().getStateMap().remove(uuid);
            dataInterface.queueUnlock(uuid);
        }
    }

    public void onServerDisconnect(ServerDisconnectEvent event) {

    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(dataCache.getStateMap().get(player.getUniqueId()) == DataCache.PlayerState.READY && player.getServer() != null) {
            dataInterface.sendConnectData(player);
        } else {
            plugin.getProxy().getScheduler().schedule(plugin, new DelayedConnectTask(0, player), 200, TimeUnit.MILLISECONDS);
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        plugin.getLogger().info(event.getTag());
        if("DataCord".equals(event.getTag())) {
            dataInterface.receivePlayerData(event.getData());
        }
    }

    private class DelayedSaveTask implements Runnable {
        private int tries;
        private ProxiedPlayer player;

        public DelayedSaveTask(int tries, ProxiedPlayer player) {
            this.tries = tries;
            this.player = player;
        }

        @Override
        public void run() {
            if(dataCache.getStateMap().get(player.getUniqueId()) == DataCache.PlayerState.READY) {
                dataInterface.queueSave(player, true);
                dataCache.getDataMap().remove(player.getUniqueId());
                dataCache.getStateMap().remove(player.getUniqueId());
            } else if(tries < 5) {
                plugin.getProxy().getScheduler().schedule(plugin, new DelayedSaveTask(tries + 1, player), 500, TimeUnit.MILLISECONDS);
            } else {
                dataInterface.queueUnlock(player.getUniqueId());
                plugin.getLogger().severe("Failed to save player data: " + player.getUniqueId() + " " + player.getDisplayName());
            }
        }
    }

    private class DelayedConnectTask implements Runnable {
        private int tries;
        private ProxiedPlayer player;

        public DelayedConnectTask(int tries, ProxiedPlayer player) {
            this.tries = tries;
            this.player = player;
        }

        @Override
        public void run() {
            if(dataCache.getStateMap().get(player.getUniqueId()) == DataCache.PlayerState.READY && player.getServer() != null) {
                dataInterface.sendConnectData(player);
            } else if(tries < 5) {
                plugin.getProxy().getScheduler().schedule(plugin, new DelayedConnectTask(tries + 1, player), 200, TimeUnit.MILLISECONDS);
            } else {
                player.disconnect(disconnectMessage);
                plugin.getLogger().severe("Failed to read data in time: " + player.getUniqueId() + " " + player.getDisplayName());
            }
        }
    }
}

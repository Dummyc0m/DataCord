package com.dummyc0m.bungeecord.datacord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dummyc0m on 7/6/15.
 * Ideally Functional
 */
public class DataCache {
    public static final String EMPTY;

    static {
        EMPTY = new String();
    }

    private final Map<UUID, String> dataMap;
    private final Map<UUID, PlayerState> stateMap;

    public DataCache() {
        dataMap = new ConcurrentHashMap<>();
        stateMap = new ConcurrentHashMap<>();
    }

    public void cacheData(UUID uuid, String data) {
        dataMap.put(uuid, data);
    }

    public Map<UUID, String> getDataMap() {
        return dataMap;
    }

    public Set<Map.Entry<UUID, String>> getDataEntrySet() {
        return Collections.unmodifiableSet(dataMap.entrySet());
    }

    public Map<UUID, PlayerState> getStateMap() {
        return stateMap;
    }

    public enum PlayerState {
        LOADING, //Loading data
        READY, //Data loaded
        CONNECTED, //In the Server
    }
}

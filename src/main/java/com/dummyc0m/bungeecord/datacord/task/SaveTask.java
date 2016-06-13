package com.dummyc0m.bungeecord.datacord.task;

import com.dummyc0m.bungeecord.datacord.DataInterface;
import com.google.common.collect.ImmutableMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dummyc0m on 7/7/15.
 * Ideally functional
 */
public class SaveTask implements Runnable {
    private static final String SAVE;

    static {
        SAVE = "UPDATE `DataCordData` SET `Data` = ?, OnlineLock = ? WHERE `Player` = ?";
    }

    private final DataInterface dataInterface;
    private final Map<UUID, String> dataMap;
    private final int lock;

    public SaveTask(DataInterface dataInterface, UUID uuid, String data, boolean unlock) {
        if (uuid == null || data == null) {
            throw new IllegalArgumentException("UUID and Data cannot be null");
        }
        this.dataMap = ImmutableMap.of(uuid, data);
        this.dataInterface = dataInterface;
        this.lock = unlock ? 0 : 1;
    }

    public SaveTask(DataInterface dataInterface, Map<UUID, String> dataMap, boolean unlock) {
        if (dataMap.isEmpty()) {
            throw new IllegalArgumentException("DataMap is empty");
        }
        this.dataMap = dataMap;
        this.dataInterface = dataInterface;
        this.lock = unlock ? 0 : 1;
    }

    @Override
    public void run() {
        try {
            Connection connection = dataInterface.create();
            PreparedStatement statement = connection.prepareStatement(SAVE);
            for (Map.Entry<UUID, String> entry : dataMap.entrySet()) {
                statement.setString(1, entry.getValue());
                statement.setInt(2, lock);
                statement.setString(3, entry.getKey().toString());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            dataInterface.discard(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

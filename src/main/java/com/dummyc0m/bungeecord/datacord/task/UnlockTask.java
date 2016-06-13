package com.dummyc0m.bungeecord.datacord.task;

import com.dummyc0m.bungeecord.datacord.DataInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Dummyc0m on 7/7/15.
 * Ideally Functional
 */
public class UnlockTask implements Runnable {
    private static final String UNLOCK;

    static {
        UNLOCK = "UPDATE `DataCordData` SET `OnlineLock` = 0 WHERE `Player` = ?";
    }

    private final DataInterface dataInterface;
    private final UUID uuid;

    public UnlockTask(DataInterface dataInterface, UUID uuid) {
        this.dataInterface = dataInterface;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            Connection connection = dataInterface.create();
            PreparedStatement unlock = connection.prepareStatement(UNLOCK);
            unlock.setString(1, uuid.toString());
            unlock.executeUpdate();
            unlock.close();
            dataInterface.discard(connection);
            dataInterface.getCache().getStateMap().remove(uuid);
            dataInterface.getCache().getDataMap().remove(uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

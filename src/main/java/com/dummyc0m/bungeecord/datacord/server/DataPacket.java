package com.dummyc0m.bungeecord.datacord.server;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class DataPacket implements Serializable {
    private Type type;
    private UUID uuid;
    private String data;

    public DataPacket(Type type, UUID uuid, String data) {
        this.type = type;
        this.uuid = uuid;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataPacket that = (DataPacket) o;

        return type == that.type && (uuid != null ? uuid.equals(that.uuid) : that.uuid == null && (data != null ? data.equals(that.data) : that.data == null));

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    public enum Type {
        SAVE, DISCONNECT, REQUEST
    }
}

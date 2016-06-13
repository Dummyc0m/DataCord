package com.dummyc0m.bungeecord.datacord.server;

import com.dummyc0m.bungeecord.datacord.DataCache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class DataServerHandler extends ChannelInboundHandlerAdapter {
    private DataCache cache;

    public DataServerHandler(DataCache cache) {
        this.cache = cache;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof DataPacket) {
            DataPacket packet = ((DataPacket) msg);
            System.out.println(packet.getType().name());
            System.out.println(packet.getUniqueId());
            System.out.println(packet.getData());
            if (packet.getType() == DataPacket.Type.SAVE) {
                System.out.println("[DEBUG] Save Packet Received for " + packet.getUniqueId());
                cache.getDataMap().put(packet.getUniqueId(), packet.getData());
            } else if (packet.getType() == DataPacket.Type.DISCONNECT) {
                System.out.println("[DEBUG] Disconnected Packet Received for " + packet.getUniqueId());
                cache.getDataMap().put(packet.getUniqueId(), packet.getData());
                cache.getStateMap().put(packet.getUniqueId(), DataCache.PlayerState.READY);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}

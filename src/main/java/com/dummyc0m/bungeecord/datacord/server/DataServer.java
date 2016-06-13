package com.dummyc0m.bungeecord.datacord.server;

import com.dummyc0m.bungeecord.datacord.DataCache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class DataServer {
    private int port;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private DataCache cache;

    public DataServer(int port, DataCache cache) {
        this.port = port;
        this.cache = cache;
    }

    public synchronized void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingResolver(DataPacket.class.getClassLoader())), new DataServerHandler(cache));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            channel = f.channel();
            System.out.println("[DEBUG] Server Started");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        channel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}

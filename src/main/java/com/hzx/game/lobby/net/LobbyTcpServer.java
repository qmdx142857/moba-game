package com.hzx.game.lobby.net;

import com.hzx.game.common.Constant;
import com.hzx.game.lobby.net.handler.LobbyHandler;
import com.hzx.game.lobby.service.LobbyService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LobbyTcpServer implements CommandLineRunner {

    @Autowired
    private final LobbyService service;

    private static final DefaultEventExecutorGroup executor = new DefaultEventExecutorGroup((
            Runtime.getRuntime().availableProcessors() * 8)
            , new DefaultThreadFactory("eventExecutorGroup"));

    public LobbyTcpServer(LobbyService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception {
        start();
    }

    private void start() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup work = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4))
                                .addLast(new ProtobufDecoder(moba.MobaProto.Packet.getDefaultInstance()))
                                .addLast(new ProtobufEncoder())
                                .addLast(new IdleStateHandler(90, 0, 0, TimeUnit.SECONDS))
                                .addLast(executor, "lobbyHandler", new LobbyHandler(service));
                    }
                });
        b.bind(Constant.LOBBY_PORT).sync();
        System.out.println("Server listen " + Constant.LOBBY_PORT);
    }

}

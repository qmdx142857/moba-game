package com.hzx.game.battle.net;

import com.hzx.game.battle.net.handler.FrameHandler;
import com.hzx.game.common.Constant;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BattleTcpServer implements CommandLineRunner {

    private static final DefaultEventExecutorGroup executor = new DefaultEventExecutorGroup((
            Runtime.getRuntime().availableProcessors() * 8)
                    , new DefaultThreadFactory("eventExecutorGroup"));

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
                                .addLast(executor, "frameHandler", new FrameHandler());
                    }
                });
        b.bind(Constant.BATTLE_PORT).sync();
        System.out.println("Server listen " + Constant.BATTLE_PORT);
    }

}

package com.hzx.game.gateway.net.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.AttributeKey;
import moba.MobaProto;

public class RouterHandler extends SimpleChannelInboundHandler<MobaProto.Packet> {

    private final String targetHost;
    private final int targetPort;
    private Channel targetChannel;

    public RouterHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MobaProto.Packet packet) throws Exception {
        if (targetChannel != null && targetChannel.isActive()) {
            targetChannel.writeAndFlush(packet);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Bootstrap b = new Bootstrap()
                .group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4))
                                .addLast(new ProtobufDecoder(MobaProto.Packet.getDefaultInstance()))
                                .addLast(new ProtobufEncoder())
                                .addLast(new ChannelInboundHandler());
                    }
                });
        this.targetChannel = b.connect(targetHost, targetPort).sync().channel();
        ctx.channel().attr(AttributeKey.<Channel>valueOf("client")).set(targetChannel);
        targetChannel.attr(AttributeKey.<Channel>valueOf("client")).set(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (targetChannel != null) targetChannel.close();
    }

}

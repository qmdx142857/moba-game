package com.hzx.game.gateway.net.handler;

import com.hzx.game.common.Constant;
import com.hzx.game.gateway.net.client.InternalClient;
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
            if (packet.hasLoginReq()) {
                // 转发给 大厅服 9000
                forwardToLobby(packet, ctx.channel());
            } else if (packet.hasMatchReq()) {
                // 转发给 匹配服 9003
                forwardToMatch(packet, ctx.channel());
            } else if (packet.hasFrameMsg()) {
                // 转发给 战斗服 9002
                forwardToBattle(packet, ctx.channel());
            }
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
        if (targetChannel != null) {
            targetChannel.close();
        }
    }

    private void forwardToLobby(MobaProto.Packet pkt, Channel clientChannel) {
        new InternalClient("lobby", Constant.LOBBY_PORT).send(pkt, clientChannel);
    }

    private void forwardToMatch(MobaProto.Packet pkt, Channel clientChannel) {
        new InternalClient("match", Constant.MATCH_PORT).send(pkt, clientChannel);
    }

    private void forwardToBattle(MobaProto.Packet pkt, Channel clientChannel) {
        new InternalClient("battle", Constant.BATTLE_PORT).send(pkt, clientChannel);
    }

}

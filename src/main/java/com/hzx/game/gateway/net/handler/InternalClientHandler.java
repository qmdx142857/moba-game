package com.hzx.game.gateway.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.Channel;
import moba.MobaProto;

public class InternalClientHandler extends SimpleChannelInboundHandler<MobaProto.Packet> {

    private final Channel clientChannel;   // 回写给 9001

    public InternalClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MobaProto.Packet pkt) {
        // 回写给 9001
        clientChannel.writeAndFlush(pkt);
    }
}
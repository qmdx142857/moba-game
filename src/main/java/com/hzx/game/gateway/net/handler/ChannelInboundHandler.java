package com.hzx.game.gateway.net.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

public class ChannelInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel client = ctx.channel().attr(AttributeKey.<Channel>valueOf("client")).get();
        if (client != null) {
            client.writeAndFlush(msg);
        }
    }

}

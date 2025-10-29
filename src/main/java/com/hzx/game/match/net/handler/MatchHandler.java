package com.hzx.game.match.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import moba.MobaProto;
import org.springframework.stereotype.Component;

@Component
public class MatchHandler extends SimpleChannelInboundHandler<MobaProto.Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MobaProto.Packet pkt) {
        if (!pkt.hasMatchReq()) {
            return;   // 不是匹配请求，直接返回
        }

        MobaProto.MatchReq req = pkt.getMatchReq();
        System.out.println("收到匹配请求 uid=" + req.getUid() + ",mode=" + req.getMode());
        ctx.writeAndFlush(MobaProto.Packet.newBuilder()
                .setMatchRsp(MobaProto.MatchRsp.newBuilder().setOk(true)).build());

    }

}

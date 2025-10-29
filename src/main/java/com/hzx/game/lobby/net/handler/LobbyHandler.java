package com.hzx.game.lobby.net.handler;

import com.hzx.game.lobby.service.LobbyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import moba.MobaProto;
import org.springframework.stereotype.Component;

@Component
public class LobbyHandler extends SimpleChannelInboundHandler<MobaProto.Packet> {

    private final LobbyService service;

    public LobbyHandler(LobbyService service) {
        this.service = service;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MobaProto.Packet packet) throws Exception {
        if (packet.hasLoginReq()) {
            MobaProto.LoginRsp rsp = service.login(packet.getLoginReq());
            ctx.writeAndFlush(MobaProto.Packet.newBuilder().setLoginRsp(rsp).build());
        }
        if (packet.hasMatchReq()) {
            service.enqueueMatch(packet.getMatchReq());
            ctx.writeAndFlush(MobaProto.Packet.newBuilder()
                    .setMatchRsp(MobaProto.MatchRsp.newBuilder().setOk(true)).build());
        }
    }
}

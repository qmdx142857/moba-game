package com.hzx.game.battle.net.handler;

import com.hzx.game.battle.dto.Player;
import com.hzx.game.battle.manager.PlayerChannelManager;
import com.hzx.game.battle.service.BattleService;
import com.hzx.game.common.Constant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.Getter;
import moba.MobaProto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class FrameHandler extends SimpleChannelInboundHandler<MobaProto.FrameMsg> {

    private final Map<Long, Player> players = new ConcurrentHashMap<>();

    @Resource
    private BattleService battleService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MobaProto.FrameMsg msg) throws Exception {
        battleService.dealPlayerSnap(players, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        long uid = System.nanoTime();
        players.put(uid, new Player(uid, Constant.DEFAULT_HP));
        ctx.channel().attr(AttributeKey.<Long>valueOf("uid")).set(uid);
        PlayerChannelManager.put(uid, ctx.channel());
        System.out.println("Battle enter uid=" + uid);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long uid = ctx.channel().attr(AttributeKey.<Long>valueOf("uid")).get();
        if (uid != null) {
            players.remove(uid);
            PlayerChannelManager.remove(uid);
        }
    }

}

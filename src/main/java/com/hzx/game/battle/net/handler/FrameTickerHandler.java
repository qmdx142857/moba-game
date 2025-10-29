package com.hzx.game.battle.net.handler;

import com.hzx.game.battle.dto.Player;
import com.hzx.game.battle.service.BattleService;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import moba.MobaProto;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FrameTickerHandler {

    @Getter
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final AtomicInteger frameId = new AtomicInteger(0);

    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

    private final FrameHandler frameHandler;
    
    @Resource
    private BattleService battleService;

    public FrameTickerHandler(FrameHandler handler) {
        this.frameHandler = handler;
    }

    @PostConstruct
    public void start() {
        timer.scheduleAtFixedRate(this::tick, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        MobaProto.FrameMsg.Builder builder = MobaProto.FrameMsg.newBuilder().setFrameId(frameId.incrementAndGet());
        for (Player p : frameHandler.getPlayers().values()) {
            battleService.dealFrameMsg(p, builder);
        }
        channels.writeAndFlush(builder.build());
    }

}

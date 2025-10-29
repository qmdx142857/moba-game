package com.hzx.game.battle.dto;

import com.hzx.game.common.Constant;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Room {

    @Getter
    private final long token;
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Map<Long, Player> players = new ConcurrentHashMap<>();

    public Room(long token, List<Long> uids) {
        this.token = token;
        uids.forEach(uid -> players.put(uid, new Player(uid, Constant.DEFAULT_HP)));
    }

    public boolean isGameOver() {
        return players.values().stream().filter(Player::isAlive).count() <= 1;
    }

    public List<Long> getWinners() {
        return players.values().stream()
                .filter(Player::isAlive)
                .map(p -> p.uid)
                .toList();
    }

    public List<Long> getAllPlayers() {
        return new ArrayList<>(players.keySet());
    }

    public void broadcast(Object msg) {
        channels.writeAndFlush(msg);
    }

    public void addChannel(Channel ch, Long uid) {
        channels.add(ch);
        ch.attr(AttributeKey.<Long>valueOf("uid")).set(uid);
    }

}

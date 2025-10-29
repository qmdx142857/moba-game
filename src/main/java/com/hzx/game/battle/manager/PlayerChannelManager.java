package com.hzx.game.battle.manager;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerChannelManager {

    private static final Map<Long, Channel> channelMap = new ConcurrentHashMap<>();

    public static void put(Long uid, Channel ch) {
        channelMap.put(uid, ch);
    }

    public static Channel get(Long uid) {
        return channelMap.get(uid);
    }

    public static void remove(Long uid) {
        channelMap.remove(uid);
    }
}

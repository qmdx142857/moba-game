package com.hzx.game.battle.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.hzx.game.battle.dto.Room;
import com.hzx.game.battle.manager.PlayerChannelManager;
import com.hzx.game.battle.manager.RoomManager;
import com.hzx.game.common.Constant;
import com.hzx.game.common.RedisKey;
import io.netty.channel.Channel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import moba.MobaProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class BattleMatchListener {

    @Resource
    private StringRedisTemplate redisTemplate;

    private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void start() {
        timer.scheduleAtFixedRate(this::pollResult, 0, 100, TimeUnit.MILLISECONDS);
        System.out.println("战斗服监听 Redis 匹配结果");
    }

    private void pollResult() {
        // 非阻塞 pop
        String data = redisTemplate.opsForList().leftPop(RedisKey.MATCH_RESULT);
        if (data == null) {
            return;
        }

        try {
            MobaProto.Result result = JSONObject.parseObject(JSON.toJSONString(data), MobaProto.Result.class);
            System.out.println("战斗服收到房间 token=" + result.getRoomToken() + " 玩家=" + result.getPlayersList());

            // 1. 创建房间（内存 Map 即可）
            Room room = RoomManager.addRoom(new Room(result.getRoomToken(), result.getPlayersList()));

            // 2. 让客户端连 9002（提前存的 Channel<uid>）
            for (Long uid : result.getPlayersList()) {
                Channel ch = PlayerChannelManager.get(uid);
                room.addChannel(ch, uid);
                if (ch != null && ch.isActive()) {
                    ch.writeAndFlush(MobaProto.Packet.newBuilder()
                            .setMatchRsp(MobaProto.MatchRsp.newBuilder()
                                    .setOk(true)
                                    .setBattleIp(Constant.BATTLE_HOST)
                                    .setBattlePort(Constant.BATTLE_PORT)
                                    .setRoomToken(result.getRoomToken()))
                            .build());
                } else {
                    System.out.println("玩家 " + uid + " 未连战斗服，跳过");
                }
            }
        } catch (Exception e) {
            System.err.println("解析匹配结果失败: " + e.getMessage());
        }
    }

}

package com.hzx.game.battle.listener;

import com.alibaba.fastjson.JSON;
import com.hzx.game.battle.dto.Room;
import com.hzx.game.battle.manager.RoomManager;
import com.hzx.game.common.RedisKey;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import moba.MobaProto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class BattleEndListener {

    @Resource
    private StringRedisTemplate redis;
    private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void start() {
        timer.scheduleAtFixedRate(this::pollEnd, 0, 1000, TimeUnit.MILLISECONDS);
        System.out.println("战斗结束监听器启动");
    }

    /**
     * 战斗结束条件：房间内存判断（存活人数 <= 1）
     */
    private void pollEnd() {
        // 遍历所有房间（内存 Map<roomToken, Room>）
        for (Room room : RoomManager.getRooms()) {
            if (room.isGameOver()) {
                List<Long> winners = room.getWinners();
                MobaProto.BattleEnd.Builder builder = MobaProto.BattleEnd.newBuilder();
                builder.setRoomToken(room.getToken())
                        .addAllPlayers(room.getAllPlayers())   // 所有人
                        .addAllWinners(winners)                // 胜利者
                        .build();

                // 1. 广播给客户端
                room.broadcast(builder.build());

                // 2. 写回 Redis（大厅/统计服订阅）
                redis.opsForList().rightPush(RedisKey.BATTLE_END, JSON.toJSONString(builder));

                // 3. 销毁房间
                RoomManager.removeRoom(room.getToken());
                System.out.println("房间 " + room.getToken() + " 已结束，胜者=" + winners);
            }
        }
    }

}

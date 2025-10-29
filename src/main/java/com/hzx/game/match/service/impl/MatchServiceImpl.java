package com.hzx.game.match.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hzx.game.common.Constant;
import com.hzx.game.common.RedisKey;
import com.hzx.game.match.service.MatchService;
import jakarta.annotation.PostConstruct;
import moba.MobaProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void start() {
        timer.scheduleAtFixedRate(this::pollAndMatch, 0, 200, TimeUnit.MILLISECONDS);
        System.out.println("MatchServer 启动，每 200ms 拉一批");
    }

    private void pollAndMatch() {
        List<String> list = redisTemplate.opsForList().range(RedisKey.MATCH_REQ, 0, 199);
        if (list == null || list.isEmpty()) {
            return;
        }
        redisTemplate.opsForList().trim(RedisKey.MATCH_REQ, list.size(),  -1);

        List<MobaProto.MatchReq> batch = new ArrayList<>();
        for (String s : list) {
            batch.add(JSONObject.parseObject(JSON.toJSONString(s), MobaProto.MatchReq.class));
        }

        List<MobaProto.Result> res = match(batch);

        res.forEach(r -> {
            // 写回 Redis 列表，战斗服订阅
            redisTemplate.opsForList().rightPush(RedisKey.MATCH_RESULT, JSON.toJSONString(r));
            System.out.println("匹配完成 token=" + r.getRoomToken() + " 玩家=" + r.getPlayersList());
        });
    }

    private List<MobaProto.Result> match(List<MobaProto.MatchReq> batch) {
        Map<Integer, List<MobaProto.MatchReq>> map = new HashMap<>();
        for (MobaProto.MatchReq matchReq : batch) {
            map.computeIfAbsent(matchReq.getMode(), k -> new ArrayList<>()).add(matchReq);
        }

        List<MobaProto.Result> res = new ArrayList<>();
        map.forEach((mode, list) -> {
            int size = mode == 1 ? 2 : 4; // 1v1=2  5v5=4
            for (int i = 0; i + size <= list.size(); i += size) {
                List<Long> players = list.subList(i, i + size).stream().map(MobaProto.MatchReq::getUid).toList();
                long token = System.nanoTime();
                res.add(MobaProto.Result.newBuilder()
                        .setRoomToken(token)
                        .addAllPlayers(players)
                        .setBattleIp(Constant.BATTLE_HOST)
                        .setBattlePort(Constant.BATTLE_PORT)
                        .build());
            }
        });

        return res;
    }

}

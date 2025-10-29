package com.hzx.game.lobby.service.impl;

import com.alibaba.fastjson.JSON;
import com.hzx.game.common.Constant;
import com.hzx.game.common.RedisKey;
import com.hzx.game.lobby.service.LobbyService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LobbyServiceImpl implements LobbyService {

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public moba.MobaProto.LoginRsp login(moba.MobaProto.LoginReq loginReq) {
        // 验证

        return moba.MobaProto.LoginRsp.newBuilder()
                .setOk(true)
                .setLobbyIp(Constant.LOBBY_HOST)
                .setLobbyPort(Constant.LOBBY_PORT)
                .build();
    }

    @Override
    public void enqueueMatch(moba.MobaProto.MatchReq matchReq) {
        redisTemplate.opsForList().rightPush(RedisKey.MATCH_REQ, JSON.toJSONString(matchReq));
    }

}

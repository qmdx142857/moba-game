package com.hzx.game.battle.service.impl;

import com.hzx.game.battle.dto.Player;
import com.hzx.game.battle.service.BattleService;
import moba.MobaProto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BattleServiceImpl implements BattleService {

    @Override
    public void dealPlayerSnap(Map<Long, Player> players, MobaProto.FrameMsg msg) {
        List<MobaProto.PlayerSnap> list = msg.getPlayersList();
        if (list.isEmpty()) {
            return;
        }
        for (MobaProto.PlayerSnap playerSnap : list) {
            Player player = players.get(playerSnap.getUid());
            if (player != null) {
                player.x += playerSnap.getX();
                player.y += playerSnap.getY();

                // 死亡逻辑
                player.hp--;
            }
        }

    }

    @Override
    public void dealFrameMsg(Player p, MobaProto.FrameMsg.Builder builder) {
        builder.addPlayers(MobaProto.PlayerSnap.newBuilder()
                .setUid(p.uid)
                .setX(p.x)
                .setY(p.y));
    }

}

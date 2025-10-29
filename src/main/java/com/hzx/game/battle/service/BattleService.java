package com.hzx.game.battle.service;

import com.hzx.game.battle.dto.Player;
import moba.MobaProto;

import java.util.Map;

public interface BattleService {

    void dealPlayerSnap(Map<Long, Player> players, MobaProto.FrameMsg msg);

    void dealFrameMsg(Player p, MobaProto.FrameMsg.Builder builder);

}

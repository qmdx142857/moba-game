package com.hzx.game.lobby.service;

import moba.MobaProto;

public interface LobbyService {

    MobaProto.LoginRsp login(MobaProto.LoginReq loginReq);

    void enqueueMatch(MobaProto.MatchReq matchReq);

}

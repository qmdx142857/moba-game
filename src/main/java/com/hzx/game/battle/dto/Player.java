package com.hzx.game.battle.dto;

public class Player {
    public final long uid;
    public float x;
    public float y;
    public int hp;

    public Player(long uid, int hp) {
        this.uid = uid;
        this.hp = hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

}

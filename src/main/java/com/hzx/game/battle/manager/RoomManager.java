package com.hzx.game.battle.manager;

import com.hzx.game.battle.dto.Room;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RoomManager {

    private static final Map<Long, Room> roomMap = new ConcurrentHashMap<>();

    public static Room addRoom(Room room) {
        roomMap.put(room.getToken(), room);
        return room;
    }

    public static Room getRoom(long token) {
        return roomMap.get(token);
    }

    public static void removeRoom(long token) {
        roomMap.remove(token);
    }

    public static Iterable<Room> getRooms() {
        return roomMap.values();
    }

}

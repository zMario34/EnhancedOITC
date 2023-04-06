package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.io.Serializable;

@Data
public class GameConnectPacket implements Packet, Serializable {

    private final String server;
    private final String arenaName;

    private final int maxPlayers;

    @Override
    public String getChannel() {
        return "enhancedoitc:update";
    }
}

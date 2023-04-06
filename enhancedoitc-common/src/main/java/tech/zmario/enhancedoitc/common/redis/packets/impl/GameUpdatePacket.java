package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.io.Serializable;

@Data
public class GameUpdatePacket implements Packet, Serializable {

    private final String server;
    private final String arenaName;

    private final GameState gameState;

    private final int players;

    @Override
    public String getChannel() {
        return "enhancedoitc:update";
    }
}

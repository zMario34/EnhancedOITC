package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.enums.GameState;

@Data
public class GameUpdatePacket {

    private final String server;
    private final String arenaName;

    private final GameState gameState;

    private final int players;

}

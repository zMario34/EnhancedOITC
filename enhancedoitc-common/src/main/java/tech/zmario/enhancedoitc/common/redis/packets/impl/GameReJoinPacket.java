package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.util.UUID;

@Data
public class GameReJoinPacket implements Packet {

    private final String playerName;
    private final UUID playerUuid;

    private final String arenaName;

    @Override
    public String getChannel() {
        return "enhancedoitc:connection";
    }
}

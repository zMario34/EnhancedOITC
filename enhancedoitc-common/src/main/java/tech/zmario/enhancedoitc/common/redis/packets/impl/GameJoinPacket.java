package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.util.UUID;

@Data
public class GameJoinPacket implements Packet {

    private final UUID playerUuid;

    private final String server;
    private final String arenaName;

    @Override
    public String getChannel() {
        return "enhancedoitc:connection";
    }
}

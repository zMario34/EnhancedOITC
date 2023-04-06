package tech.zmario.enhancedoitc.common.redis.packets.impl;

import lombok.Data;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.io.Serializable;
import java.util.UUID;

@Data
public class PlayerJoinPacket implements Packet, Serializable {

    private final UUID playerUuid;

    private final String server;
    private final String arenaName;

    @Override
    public String getChannel() {
        return "enhancedoitc:connection";
    }
}

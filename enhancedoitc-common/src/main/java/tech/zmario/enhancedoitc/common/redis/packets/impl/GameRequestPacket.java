package tech.zmario.enhancedoitc.common.redis.packets.impl;

import tech.zmario.enhancedoitc.common.redis.packets.Packet;

import java.io.Serializable;

public class GameRequestPacket implements Packet, Serializable {

    @Override
    public String getChannel() {
        return "enhancedoitc:connection";
    }
}

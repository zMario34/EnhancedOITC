package tech.zmario.enhancedoitc.connector.listeners.redis;

import lombok.RequiredArgsConstructor;
import tech.zmario.enhancedoitc.common.redis.listener.PubSubListener;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameConnectPacket;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameDisconnectPacket;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameUpdatePacket;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.objects.GameArena;

@RequiredArgsConstructor
public class GameUpdateListener extends PubSubListener<String, Object> {

    private final OITCConnector plugin;

    @Override
    public void message(String channel, Object object) {
        Packet packet = (Packet) object;

        if (packet instanceof GameUpdatePacket) {
            GameUpdatePacket updatePacket = (GameUpdatePacket) packet;

            plugin.getArenaManager().updateArena(updatePacket);
        } else if (packet instanceof GameConnectPacket) {
            GameConnectPacket connectPacket = (GameConnectPacket) packet;
            GameArena arena = new GameArena(plugin, connectPacket.getServer(),
                    connectPacket.getArenaName(), connectPacket.getMaxPlayers());

            plugin.getArenaManager().addArena(arena);
        } else if (packet instanceof GameDisconnectPacket) {
            GameDisconnectPacket cancelPacket = (GameDisconnectPacket) packet;

            plugin.getArenaManager().removeArena(cancelPacket.getArenaName());
        }
    }
}

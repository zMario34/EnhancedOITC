package tech.zmario.enhancedoitc.game.listeners.redis;

import lombok.RequiredArgsConstructor;
import tech.zmario.enhancedoitc.common.redis.listener.PubSubListener;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameRequestPacket;
import tech.zmario.enhancedoitc.common.redis.packets.impl.PlayerJoinPacket;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;

@RequiredArgsConstructor
public class RedisConnectionListener extends PubSubListener<String, Object> {

    private final EnhancedOITC plugin;

    @Override
    public void message(String channel, Object object) {
        if (!(object instanceof Packet)) return;
        Packet packet = (Packet) object;

        if (packet instanceof PlayerJoinPacket) {
            PlayerJoinPacket playerJoinPacket = (PlayerJoinPacket) packet;

            plugin.debug("Received join packet for player " + playerJoinPacket.getPlayerUuid() + " in arena " + playerJoinPacket.getArenaName());

            plugin.getArenaManager().getArena(playerJoinPacket.getArenaName())
                    .ifPresentOrElse(arena -> plugin.getArenaManager().getArenasByPlayer().put(playerJoinPacket.getPlayerUuid(), arena),
                            () -> plugin.getLogger().warning("Cannot find arena " + playerJoinPacket.getArenaName()));
        } else if (packet instanceof GameRequestPacket) {
            plugin.debug("Received game request packet");

            plugin.getArenaManager().getArenas().values().forEach(Arena::sendConnectPacket);
        }
    }
}

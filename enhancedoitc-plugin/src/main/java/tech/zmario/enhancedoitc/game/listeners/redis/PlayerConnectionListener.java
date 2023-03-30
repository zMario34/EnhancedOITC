package tech.zmario.enhancedoitc.game.listeners.redis;

import lombok.RequiredArgsConstructor;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.common.redis.listener.PubSubListener;
import tech.zmario.enhancedoitc.common.redis.packets.Packet;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameJoinPacket;

@RequiredArgsConstructor
public class PlayerConnectionListener extends PubSubListener<String, Object> {

    private final EnhancedOITC plugin;

    @Override
    public void message(String channel, Object object) {
        if (!(object instanceof Packet)) return;
        Packet packet = (Packet) object;

        if (packet instanceof GameJoinPacket) {
            GameJoinPacket gameJoinPacket = (GameJoinPacket) packet;

            plugin.getArenaManager().getArena(gameJoinPacket.getArenaName())
                    .ifPresentOrElse(arena -> plugin.getArenaManager().getArenasByPlayer().put(gameJoinPacket.getPlayerUuid(), arena),
                            () -> plugin.getLogger().warning("Cannot find arena " + gameJoinPacket.getArenaName()));
        }
    }
}

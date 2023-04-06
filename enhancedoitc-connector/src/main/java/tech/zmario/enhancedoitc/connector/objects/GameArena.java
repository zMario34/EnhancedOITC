package tech.zmario.enhancedoitc.connector.objects;

import lombok.Data;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.redis.packets.impl.PlayerJoinPacket;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.enums.MessagesConfiguration;

import java.util.Collections;

@Data
public class GameArena {

    private final OITCConnector plugin;

    private final String server;
    private final String name;
    private final int maxPlayers;
    private GameState gameState = GameState.WAITING;
    private int players = 0;

    public void addPlayer(Player player) {
        if (gameState == GameState.RESTARTING || gameState == GameState.PLAYING) {
            MessagesConfiguration.JOIN_DENIED.send(player, plugin);
            return;
        }

        if (players >= maxPlayers) {
            MessagesConfiguration.GAME_FULL.send(player, plugin);
            return;
        }

        PlayerJoinPacket joinPacket = new PlayerJoinPacket(player.getUniqueId(), server, name);

        plugin.getRedisHandler().publish(joinPacket.getChannel(), joinPacket);
        Utils.sendPlayersToServer(plugin, server, Collections.singletonList(player));
    }

    public void removePlayer(Player player) {

    }
}

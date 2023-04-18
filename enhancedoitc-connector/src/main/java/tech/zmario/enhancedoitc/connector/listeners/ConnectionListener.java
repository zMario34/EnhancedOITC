package tech.zmario.enhancedoitc.connector.listeners;

import fr.minuskube.netherboard.Netherboard;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.zmario.enhancedoitc.common.objects.User;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.enums.MessagesConfiguration;

import java.util.UUID;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {

    private final OITCConnector plugin;

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID uuid = event.getUniqueId();

        plugin.getSqlManager().getUser(uuid).thenAccept(sqlUserOptional -> plugin.getRedisHandler().getUser(uuid).thenAccept(redisUserOptional -> {
            if (sqlUserOptional.isEmpty()) {
                User user = new User(uuid, 0, 0, 0, 0);

                plugin.getSqlManager().createUser(uuid);
                plugin.getRedisHandler().updateUser(user);
                plugin.getStorage().addUser(user);
                return;
            }
            User sqlUser = sqlUserOptional.get();

            if (redisUserOptional.isEmpty()) {
                plugin.getRedisHandler().updateUser(sqlUser);
                plugin.getStorage().addUser(sqlUser);
                return;
            }
            User redisUser = redisUserOptional.get();

            plugin.getSqlManager().updateUser(redisUser);
            plugin.getStorage().addUser(redisUser);
        })).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);
        Netherboard.instance().createBoard(player, MessagesConfiguration.SCOREBOARD_TITLE.getString(plugin, player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);
        plugin.getStorage().removeUser(player.getUniqueId());
    }
}

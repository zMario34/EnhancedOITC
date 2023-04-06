package tech.zmario.enhancedoitc.game.listeners.bukkit;

import fr.minuskube.netherboard.Netherboard;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler(ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<Arena> arenaOptional = plugin.getArenaManager().getArena(uuid);

        plugin.debug("Player " + player.getName() + " is logging in...");
        if (arenaOptional.isEmpty() && !player.hasPermission("enhancedoitc.admin")) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Cannot load your data.");
            return;
        }

        plugin.debug("Player " + player.getName() + " is logging in... (2)");

        if (arenaOptional.isEmpty()) return;

        plugin.debug("Player " + player.getName() + " was added to the storage.");
        plugin.getRedisHandler().getUser(uuid).thenAccept(user -> plugin.getStorage().addUser(user));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);

        Netherboard.instance().createBoard(player, MessagesConfiguration.SCOREBOARD_TITLE.getString(plugin, player));

        plugin.getArenaManager().getArena(player.getUniqueId()).ifPresentOrElse(arena -> {
            arena.addPlayer(player);

            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.getUniqueId().equals(player.getUniqueId()) ||
                        arena.getPlayers().contains(online.getUniqueId())) continue;
                online.hidePlayer(player);
                player.hidePlayer(online);
            }
        }, () -> {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.getUniqueId().equals(player.getUniqueId())) continue;
                online.hidePlayer(player);
                player.hidePlayer(online);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);

        plugin.getArenaManager().getArena(player.getUniqueId()).ifPresent(arena -> arena.removePlayer(player));
    }
}

package tech.zmario.enhancedoitc.game.listeners.system;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler (ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<Arena> arenaOptional = plugin.getArenaManager().getArena(uuid);

        if (arenaOptional.isEmpty()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Cannot load your data.");
            return;
        }
        Arena arena = arenaOptional.get();

        arena.addPlayer(player);
    }
}

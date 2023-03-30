package tech.zmario.enhancedoitc.game.listeners.system;

import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;

@RequiredArgsConstructor
public class WorldLoadListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        Arena arena = plugin.getArenaManager().getEnableQueue().get(worldName);

        if (arena == null) return;

        arena.load(world);
    }
}

package tech.zmario.enhancedoitc.game.workload.impl;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.game.workload.Workload;

@RequiredArgsConstructor
public class TeleportablePlayerWorkload implements Workload {

    private final Player player;
    private final Location location;

    @Override
    public void compute() {
        player.teleport(location);
    }
}
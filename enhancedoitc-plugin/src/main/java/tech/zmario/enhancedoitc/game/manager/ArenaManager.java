package tech.zmario.enhancedoitc.game.manager;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.game.workload.impl.TeleportablePlayerWorkload;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.objects.PlayerCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Getter
public class ArenaManager {

    protected final EnhancedOITC plugin;

    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> arenasByPlayer = new HashMap<>();

    private final Map<String, Arena> enableQueue = new ConcurrentHashMap<>();

    private final ArrayListMultimap<Arena, PlayerCache> leaderboards = ArrayListMultimap.create();

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public Optional<Arena> getArena(UUID uuid) {
        return Optional.ofNullable(arenasByPlayer.get(uuid));
    }

    public boolean isInArena(Player player) {
        return arenasByPlayer.containsKey(player.getUniqueId());
    }

    public int getOnlinePlayers() {
        return arenasByPlayer.size();
    }

    public void addInQueue(Arena arena) {
        enableQueue.put(arena.getName(), arena);
    }

    public void removeFromQueue(Arena arena) {
        enableQueue.remove(arena.getName());
    }

    public void startGame(Arena arena) {
        Queue<Location> spawnLocations = new LinkedList<>(arena.getSpawnLocations());

        arena.setGameState(GameState.PLAYING);

        arena.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            Location location;

            if ((location = spawnLocations.poll()) == null) {
                player.sendMessage(ChatColor.RED + "There are no spawn locations available (set more!). Please contact an administrator!");
                return;
            }

            plugin.getWorkloadThread().addWorkload(new TeleportablePlayerWorkload(player, location));

            MessagesConfiguration.GAME_STARTED.send(player, plugin);
            MessagesConfiguration.TITLE_GAME_STARTED.send(player, plugin);
            MessagesConfiguration.SOUND_GAME_STARTED.playSound(player, plugin);
        });

    }
}

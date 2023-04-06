package tech.zmario.enhancedoitc.game.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameUpdatePacket;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.game.objects.Kit;
import tech.zmario.enhancedoitc.game.workload.impl.TeleportablePlayerWorkload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Getter
public class ArenaManager {

    protected final EnhancedOITC plugin;

    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> arenasByPlayer = new HashMap<>();

    private final Map<String, Arena> enableQueue = new ConcurrentHashMap<>();

    private int currentArenaId = 1;

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    public Optional<Arena> getArena(UUID uuid) {
        return Optional.ofNullable(arenasByPlayer.get(uuid));
    }

    public void addInQueue(Arena arena) {
        enableQueue.put(arena.getName(), arena);
    }

    public void removeFromQueue(Arena arena) {
        enableQueue.remove(arena.getName());
    }

    public void startGame(Arena arena) {
        Kit kit = arena.getKit();
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

            player.getInventory().setContents(kit.getItems());
            player.getInventory().setArmorContents(kit.getArmor());

            MessagesConfiguration.GAME_STARTED.send(player, plugin);
            MessagesConfiguration.TITLE_GAME_STARTED.sendTitle(player, plugin);
            MessagesConfiguration.SOUND_GAME_STARTED.playSound(player, plugin);
        });


        GameUpdatePacket packet = new GameUpdatePacket(SettingsConfiguration.SERVER_NAME.getString(plugin),
                arena.getName(), arena.getGameState(), arena.getPlayers().size());

        plugin.getRedisHandler().publish(packet.getChannel(), packet);
    }

    public void disable() {
        arenas.values().forEach(Arena::sendDisconnectPacket);
        arenas.clear();
    }

    public int getNextArenaId() {
        return currentArenaId++;
    }
}

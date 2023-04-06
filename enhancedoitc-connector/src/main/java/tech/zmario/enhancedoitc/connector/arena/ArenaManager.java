package tech.zmario.enhancedoitc.connector.arena;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameUpdatePacket;
import tech.zmario.enhancedoitc.connector.objects.GameArena;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class ArenaManager {

    private final ArrayListMultimap<String, GameArena> activeArenas = ArrayListMultimap.create();

    public void addArena(GameArena arena) {
        activeArenas.put(arena.getServer(), arena);
    }

    public void removeArena(String arena) {
        activeArenas.get(arena).removeIf(gameArena -> gameArena.getName().equals(arena));
    }

    public Optional<GameArena> getArena(String server, String arenaName) {
        return activeArenas.get(server).stream()
                .filter(arena -> arena.getName().equals(arenaName))
                .findFirst();
    }

    public void updateArena(GameUpdatePacket packet) {
        getArena(packet.getServer(), packet.getArenaName()).ifPresent(arena -> {
            arena.setGameState(packet.getGameState());
            arena.setPlayers(packet.getPlayers());
        });
    }

    public List<GameArena> getFreeArenas() {
        return activeArenas.values().stream()
                .filter(arena -> arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING)
                .filter(arena -> arena.getPlayers() < arena.getMaxPlayers())
                .collect(Collectors.toList());
    }

    public Optional<GameArena> getRandomArena(String name) {
        return activeArenas.values().stream()
                .filter(arena -> (arena.getGameState() == GameState.WAITING ||
                        arena.getGameState() == GameState.STARTING) &&
                        (name == null || arena.getName().contains(name)) &&
                        arena.getPlayers() + 1 <= arena.getMaxPlayers())
                .findFirst();
    }
}

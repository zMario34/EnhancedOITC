package tech.zmario.enhancedoitc.connector.arena;

import lombok.Getter;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameUpdatePacket;
import tech.zmario.enhancedoitc.connector.objects.GameArena;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ArenaManager {

    private final Map<String, GameArena> activeArenas = new HashMap<>();

    public void addArena(GameArena arena) {
        activeArenas.put(arena.getName(), arena);
    }

    public void removeArena(GameArena arena) {
        activeArenas.remove(arena.getName());
    }

    public Optional<GameArena> getArena(String name) {
        return Optional.ofNullable(activeArenas.get(name));
    }

    public void updateArena(GameUpdatePacket packet) {
        getArena(packet.getArenaName()).ifPresent(arena -> {
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
}

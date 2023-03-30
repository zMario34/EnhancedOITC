package tech.zmario.enhancedoitc.game.tasks;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.common.objects.Placeholder;

import java.util.UUID;

public class GameStartingTask extends BukkitRunnable {

    private final Arena arena;
    private final EnhancedOITC plugin;

    @Getter
    private int countdown;

    public GameStartingTask(Arena arena) {
        this.arena = arena;
        this.plugin = arena.getPlugin();
        this.countdown = SettingsConfiguration.GAME_LOBBY_COUNTDOWN.getInt(plugin);

        runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        int players = arena.getPlayers().size();

        if (players == 0 || players < arena.getMinPlayers() && arena.getGameState() == GameState.WAITING) return;

        if (arena.getPlayers().size() < arena.getMinPlayers()) {
            arena.setGameState(GameState.WAITING);

            arena.getPlayers().forEach(uuid -> {
                Player player = plugin.getServer().getPlayer(uuid);

                MessagesConfiguration.NOT_ENOUGH_PLAYERS.send(player, plugin);
                MessagesConfiguration.SOUND_GAME_NOT_ENOUGH_PLAYERS.playSound(player, plugin);
            });

            countdown = SettingsConfiguration.GAME_LOBBY_COUNTDOWN.getInt(plugin);
            return;
        }

        if (players == arena.getMaxPlayers() && countdown > SettingsConfiguration.GAME_FULL_COUNTDOWN.getInt(plugin)) {
            countdown = SettingsConfiguration.GAME_FULL_COUNTDOWN.getInt(plugin);

            MessagesConfiguration.GAME_STARTING_FULL.broadcast(arena, plugin,
                    new Placeholder("time", countdown + ""),
                    new Placeholder("second-or-seconds", (countdown == 1 ?
                            MessagesConfiguration.PLACEHOLDER_SECOND.getString(plugin) :
                            MessagesConfiguration.PLACEHOLDER_SECONDS.getString(plugin))));
            return;
        }


        if (countdown > 1) {
            countdown--;

            if (!SettingsConfiguration.GAME_START_BROADCAST_TIMES.getList(plugin).contains(countdown + "")) return;
            Placeholder time = new Placeholder("time", countdown + "");
            Placeholder secondOrSeconds = new Placeholder("second-or-seconds", (countdown == 1 ?
                    MessagesConfiguration.PLACEHOLDER_SECOND.getString(plugin) :
                    MessagesConfiguration.PLACEHOLDER_SECONDS.getString(plugin)));

            for (UUID uuid : arena.getPlayers()) {
                Player player = plugin.getServer().getPlayer(uuid);

                MessagesConfiguration.GAME_STARTING.send(player, plugin, time, secondOrSeconds);
                MessagesConfiguration.TITLE_GAME_STARTING.sendTitle(player, plugin, time, secondOrSeconds);
                MessagesConfiguration.SOUND_GAME_STARTING.playSound(player, plugin);
            }
            return;
        }

        plugin.getArenaManager().startGame(arena);
        cancel();
    }
}

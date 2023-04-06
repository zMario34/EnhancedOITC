package tech.zmario.enhancedoitc.game.tasks;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.objects.PlayerCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScoreboardTask extends BukkitRunnable {

    private final EnhancedOITC plugin;

    @Override
    public void run() {
        String nonePlaceholder = MessagesConfiguration.PLACEHOLDER_NONE.getString(plugin, null);

        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            List<PlayerCache> topKillers = arena.getPlayerCaches().values().stream()
                    .filter(cache -> cache.getKills() > 0)
                    .sorted(Comparator.comparingInt(PlayerCache::getKills))
                    .limit(3)
                    .collect(Collectors.toList());

            List<String> lines = new ArrayList<>();
            Placeholder players = new Placeholder("players", arena.getPlayers().size() + "");
            Placeholder maxPlayers = new Placeholder("max-players", arena.getMaxPlayers() + "");
            Placeholder arenaName = new Placeholder("arena", arena.getDisplayName());

            Placeholder firstName = new Placeholder("first-name", topKillers.size() > 0 ?
                    topKillers.get(0).getName() : nonePlaceholder);
            Placeholder firstKills = new Placeholder("first-kills", topKillers.size() > 0 ?
                    topKillers.get(0).getKills() + "" : "0");
            Placeholder secondName = new Placeholder("second-name", topKillers.size() > 1 ?
                    topKillers.get(1).getName() : nonePlaceholder);
            Placeholder secondKills = new Placeholder("second-kills", topKillers.size() > 1 ?
                    topKillers.get(1).getKills() + "" : "0");
            Placeholder thirdName = new Placeholder("third-name", topKillers.size() > 2 ?
                    topKillers.get(2).getName() : nonePlaceholder);
            Placeholder thirdKills = new Placeholder("third-kills", topKillers.size() > 2 ?
                    topKillers.get(2).getKills() + "" : "0");
            Placeholder id = new Placeholder("id", arena.getId() + "");

            switch (arena.getGameState()) {
                case WAITING:
                    lines = MessagesConfiguration.SCOREBOARD_LINES_WAITING.getStringList(plugin, null,
                            id, players, maxPlayers, arenaName);
                    break;
                case STARTING:
                    lines = MessagesConfiguration.SCOREBOARD_LINES_STARTING.getStringList(plugin, null, players,
                            maxPlayers, arenaName, id,
                            new Placeholder("time", arena.getGameStartingTask().getCountdown() + ""),
                            new Placeholder("second-or-seconds", (arena.getGameStartingTask().getCountdown() == 1 ?
                                    MessagesConfiguration.PLACEHOLDER_SECOND.getString(plugin, null) :
                                    MessagesConfiguration.PLACEHOLDER_SECONDS.getString(plugin, null))));
                    break;
                case PLAYING:
                    lines = MessagesConfiguration.SCOREBOARD_LINES_PLAYING.getStringList(plugin, null,
                            id, players, maxPlayers, arenaName, firstName, firstKills,
                            secondName, secondKills, thirdName, thirdKills);
                    break;
                case RESTARTING:
                    lines = MessagesConfiguration.SCOREBOARD_LINES_ENDING.getStringList(plugin, null,
                            id, players, maxPlayers, arenaName, firstName, firstKills,
                            secondName, secondKills, thirdName, thirdKills);
                    break;
            }
            for (UUID uuid : arena.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                BPlayerBoard board = Netherboard.instance().getBoard(player);

                if (board == null) continue;

                if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    lines = lines.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.toList());
                }

                board.setAll(lines.toArray(new String[0]));
            }
        }
    }
}

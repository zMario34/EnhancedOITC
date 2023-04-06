package tech.zmario.enhancedoitc.connector.tasks;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.common.objects.User;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.enums.MessagesConfiguration;

import java.util.List;

@RequiredArgsConstructor
public class ScoreboardTask extends BukkitRunnable {

    private final OITCConnector plugin;

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BPlayerBoard board = Netherboard.instance().getBoard(player);

            if (board == null) continue;
            User user = plugin.getStorage().getUser(player.getUniqueId());

            List<String> lines = MessagesConfiguration.SCOREBOARD_LINES.getStringList(plugin, player,
                    new Placeholder("name", player.getName()),
                    new Placeholder("kills", user.getKills() + ""),
                    new Placeholder("deaths", user.getDeaths() + ""),
                    new Placeholder("wins", user.getWins() + ""),
                    new Placeholder("losses", user.getLosses() + ""));

            board.setAll(lines.toArray(new String[0]));
        }
    }
}
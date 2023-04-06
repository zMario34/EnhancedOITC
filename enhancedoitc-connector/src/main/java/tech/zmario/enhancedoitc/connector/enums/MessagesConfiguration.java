package tech.zmario.enhancedoitc.connector.enums;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.connector.OITCConnector;

import java.util.List;

@RequiredArgsConstructor
public enum MessagesConfiguration {

    COMMAND_RELOAD_SUCCESS("commands.reload-success"),
    NO_ARENAS_AVAILABLE("no-arenas-available"),
    JOIN_DENIED("join-denied"),
    GAME_FULL("join-denied-full"),
    SCOREBOARD_TITLE("scoreboard.title"),
    SCOREBOARD_LINES("scoreboard.lines"),
    COMMAND_HELP("commands.help"),
    ;

    private final String path;

    public String getString(OITCConnector plugin, Player player, Placeholder... placeholders) {
        String string = Utils.colorize(plugin.getMessages().getString(path));

        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getPlaceholder(), placeholder.getValue());
        }

        if (player != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            string = PlaceholderAPI.setPlaceholders(player, string);
        }

        return string;
    }

    public List<String> getStringList(OITCConnector plugin, Player player, Placeholder... placeholders) {
        List<String> list = plugin.getMessages().getStringList(path);

        for (Placeholder placeholder : placeholders) {
            list.replaceAll(string -> string.replace(placeholder.getPlaceholder(), placeholder.getValue()));
        }

        if (player != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            list.replaceAll(string -> PlaceholderAPI.setPlaceholders(player, string));
        }

        return list;
    }

    public void send(Player player, OITCConnector plugin, Placeholder... placeholders) {
        Object object = plugin.getMessages().get(path);

        if (object instanceof String) {
            player.sendMessage(getString(plugin, player, placeholders));
        } else if (object instanceof List<?>) {
            player.sendMessage(getStringList(plugin, player, placeholders)
                    .stream()
                    .map(Utils::colorize)
                    .toArray(String[]::new));
        }
    }
}

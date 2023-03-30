package tech.zmario.enhancedoitc.connector.enums;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.utils.Utils;

import java.util.List;

@RequiredArgsConstructor
public enum MessagesConfiguration {

    JOIN_DENIED("join-denied"),
    GAME_FULL("join-denied-full"),

    ;

    private final String path;

    public String getString(OITCConnector plugin, Placeholder... placeholders) {
        String string = Utils.colorize(plugin.getMessages().getString(path));

        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getPlaceholder(), placeholder.getValue());
        }

        return string;
    }

    public List<String> getStringList(OITCConnector plugin, Placeholder... placeholders) {
        List<String> list = plugin.getMessages().getStringList(path);

        for (Placeholder placeholder : placeholders) {
            list.replaceAll(string -> string.replace(placeholder.getPlaceholder(), placeholder.getValue()));
        }

        return list;
    }

    public void send(Player player, OITCConnector plugin, Placeholder... placeholders) {
        Object object = plugin.getMessages().get(path);

        if (object instanceof String) {
            player.sendMessage(getString(plugin, placeholders));
        } else if (object instanceof List<?>) {
            player.sendMessage(getStringList(plugin, placeholders).stream().map(Utils::colorize).toArray(String[]::new));
        }
    }
}

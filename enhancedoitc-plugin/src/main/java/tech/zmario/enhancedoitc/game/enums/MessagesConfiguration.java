package tech.zmario.enhancedoitc.game.enums;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.game.utils.Utils;

import java.util.List;

@RequiredArgsConstructor
public enum MessagesConfiguration {

    NOT_ENOUGH_PLAYERS("not-enough-players"),
    GAME_STARTING("game-starting"),
    GAME_STARTING_FULL("game-starting-full"),
    GAME_STARTED("game-started"),
    GAME_END("game-end"),

    JOIN_MESSAGE("join-message"),
    LEAVE_MESSAGE("leave-message"),
    PLAYER_DEATH("player-death"),


    TITLE_GAME_NOT_ENOUGH_PLAYERS("titles.not-enough-players"),
    TITLE_GAME_STARTING("titles.game-starting"),
    TITLE_GAME_STARTED("titles.game-started"),
    TITLE_GAME_END("titles.game-end"),

    SOUND_GAME_NOT_ENOUGH_PLAYERS("sounds.not-enough-players"),
    SOUND_GAME_STARTING("sounds.game-starting"),
    SOUND_GAME_STARTED("sounds.game-started"),
    SOUND_GAME_END("sounds.game-end"),

    PLACEHOLDER_SECOND("placeholders.second"),
    PLACEHOLDER_SECONDS("placeholders.seconds"),
    PLACEHOLDER_NONE("placeholders.none"),
    ;

    private final String path;

    public String getString(EnhancedOITC plugin, Placeholder... placeholders) {
        String string = Utils.colorize(plugin.getMessages().getString(path));

        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getPlaceholder(), placeholder.getValue());
        }

        return string;
    }

    public List<String> getStringList(EnhancedOITC plugin, Placeholder... placeholders) {
        List<String> list = plugin.getMessages().getStringList(path);

        for (Placeholder placeholder : placeholders) {
            list.replaceAll(string -> string.replace(placeholder.getPlaceholder(), placeholder.getValue()));
        }

        return list;
    }

    public void send(Player player, EnhancedOITC plugin, Placeholder... placeholders) {
        Object object = plugin.getMessages().get(path);

        if (object instanceof String) {
            player.sendMessage(getString(plugin, placeholders));
        } else if (object instanceof List<?>) {
            player.sendMessage(getStringList(plugin, placeholders).stream().map(Utils::colorize).toArray(String[]::new));
        }
    }

    public void broadcast(Arena arena, EnhancedOITC plugin, Placeholder... placeholders) {
        arena.getPlayers().forEach(uuid -> send(Bukkit.getPlayer(uuid), plugin, placeholders));

        if (!arena.getSpectators().isEmpty()) {
            arena.getSpectators().forEach(uuid -> send(Bukkit.getPlayer(uuid), plugin, placeholders));
        }
    }

    public void sendTitle(Player player, EnhancedOITC plugin, Placeholder... placeholders) {
        String path = getString(plugin, placeholders);

        String[] split = path.split(";");

        String title = split.length > 0 ? split[0] : null;
        String subtitle = split.length > 1 ? split[1] : null;
        int fadeIn = split.length > 2 ? Integer.parseInt(split[2]) : 10;
        int stay = split.length > 3 ? Integer.parseInt(split[3]) : 10;
        int fadeOut = split.length > 4 ? Integer.parseInt(split[4]) : 10;

        Utils.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public void playSound(Player player, EnhancedOITC plugin) {
        String path = getString(plugin);
        String[] split = path.split(";");

        String sound = split.length > 0 ? split[0] : null;
        float volume = split.length > 1 ? Float.parseFloat(split[1]) : 1;
        float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1;

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}

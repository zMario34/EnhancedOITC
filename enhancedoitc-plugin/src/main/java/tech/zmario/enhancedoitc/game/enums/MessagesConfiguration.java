package tech.zmario.enhancedoitc.game.enums;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.utils.PacketUtils;

import java.util.List;
import java.util.stream.Collectors;

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

    COMMAND_HELP_USER("commands.help.user"),
    COMMAND_HELP_ADMIN("commands.help.admin"),
    COMMAND_RELOAD_SUCCESS("commands.reload-success"),
    COMMAND_LEAVE_NOT_IN_ARENA("commands.leave.not-in-arena"),

    COMMAND_SETUP_IN_ARENA("commands.setup.in-arena"),
    COMMAND_SETUP_ALREADY_IN_SETUP("commands.setup.already-in-setup"),
    COMMAND_SETUP_NOT_IN_SETUP("commands.setup.not-in-setup"),

    CHAT_FORMAT_WAITING("chat-format.waiting"),
    CHAT_FORMAT_PLAYING("chat-format.playing"),
    CHAT_FORMAT_ENDING("chat-format.ending"),

    SCOREBOARD_TITLE("scoreboard.title"),
    SCOREBOARD_LINES_WAITING("scoreboard.lines.waiting"),
    SCOREBOARD_LINES_STARTING("scoreboard.lines.starting"),
    SCOREBOARD_LINES_PLAYING("scoreboard.lines.playing"),
    SCOREBOARD_LINES_ENDING("scoreboard.lines.ending"),

    TITLE_GAME_NOT_ENOUGH_PLAYERS("titles.not-enough-players"),
    TITLE_GAME_STARTING("titles.game-starting"),
    TITLE_GAME_STARTED("titles.game-started"),
    TITLE_GAME_END("titles.game-end"),

    SOUND_GAME_NOT_ENOUGH_PLAYERS("sounds.not-enough-players"),
    SOUND_GAME_STARTING("sounds.game-starting"),
    SOUND_GAME_STARTED("sounds.game-started"),
    SOUND_GAME_END("sounds.game-end"),
    SOUND_PLAYER_DEATH("sounds.player-death"),
    SOUND_PLAYER_KILL("sounds.player-kill"),

    PLACEHOLDER_SECOND("placeholders.second"),
    PLACEHOLDER_SECONDS("placeholders.seconds"),
    PLACEHOLDER_NONE("placeholders.none"),
    ;

    private final String path;

    public String getString(EnhancedOITC plugin, Player player, Placeholder... placeholders) {
        String string = Utils.colorize(plugin.getMessages().getString(path));

        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getPlaceholder(), placeholder.getValue());
        }

        if (player != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            string = PlaceholderAPI.setPlaceholders(player, string);
        }

        return string;
    }

    public List<String> getStringList(EnhancedOITC plugin, Player player, Placeholder... placeholders) {
        List<String> list = plugin.getMessages().getStringList(path).stream().map(Utils::colorize).collect(Collectors.toList());

        for (Placeholder placeholder : placeholders) {
            list.replaceAll(string -> string.replace(placeholder.getPlaceholder(), placeholder.getValue()));
        }

        if (player != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            list.replaceAll(string -> PlaceholderAPI.setPlaceholders(player, string));
        }

        return list;
    }

    public void send(Player player, EnhancedOITC plugin, Placeholder... placeholders) {
        Object object = plugin.getMessages().get(path);

        if (object instanceof String) {
            player.sendMessage(getString(plugin, player, placeholders));
        } else if (object instanceof List<?>) {
            player.sendMessage(getStringList(plugin, player, placeholders).toArray(new String[0]));
        }
    }

    public void broadcast(Arena arena, EnhancedOITC plugin, Placeholder... placeholders) {
        arena.getPlayers().forEach(uuid -> send(Bukkit.getPlayer(uuid), plugin, placeholders));
    }

    public void sendTitle(Player player, EnhancedOITC plugin, Placeholder... placeholders) {
        String path = getString(plugin, player, placeholders);

        String[] split = path.split(";");

        String title = split.length > 0 ? split[0] : "";
        String subtitle = split.length > 1 ? split[1] : "";
        int fadeIn = split.length > 2 ? Integer.parseInt(split[2]) : 0;
        int stay = split.length > 3 ? Integer.parseInt(split[3]) : 20;
        int fadeOut = split.length > 4 ? Integer.parseInt(split[4]) : 0;

        PacketUtils.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public void playSound(Player player, EnhancedOITC plugin) {
        String path = getString(plugin, player);
        String[] split = path.split(";");

        String sound = split.length > 0 ? split[0] : "LEVEL_UP";
        float volume = split.length > 1 ? Float.parseFloat(split[1]) : 1;
        float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1;

        player.playSound(player.getLocation(), Sound.valueOf(sound), volume, pitch);
    }
}

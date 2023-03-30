package tech.zmario.enhancedoitc.game.enums;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import tech.zmario.enhancedoitc.game.EnhancedOITC;

import java.util.List;

@RequiredArgsConstructor
public enum SettingsConfiguration {

    SERVER_NAME("server-name"),
    REDIS_URI("redis-uri"),

    MYSQL_HOST("mysql.host"),
    MYSQL_PORT("mysql.port"),
    MYSQL_USERNAME("mysql.username"),
    MYSQL_PASSWORD("mysql.password"),

    LOADER_TYPE("slime-loader-type"),

    LOBBY_LOCATION("lobby-location"),
    GAME_WIN_KILLS("game.win-kills"),
    GAME_LOBBY_COUNTDOWN("game.countdowns.lobby"),
    GAME_FULL_COUNTDOWN("game.countdowns.full"),
    GAME_START_COUNTDOWN("game.countdowns.start"),
    GAME_RESTART_COUNTDOWN("game.countdowns.restart"),

    GAME_START_BROADCAST_TIMES("game.start-broadcast-times"),
    ;

    private final String path;

    public String getString(Plugin plugin) {
        return plugin.getConfig().getString(path);
    }

    public int getInt(Plugin plugin) {
        return plugin.getConfig().getInt(path);
    }

    public boolean getBoolean(Plugin plugin) {
        return plugin.getConfig().getBoolean(path);
    }

    public List<String> getList(EnhancedOITC plugin) {
        return plugin.getConfig().getStringList(path);
    }
}

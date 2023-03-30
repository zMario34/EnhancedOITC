package tech.zmario.enhancedoitc.connector.enums;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public enum SettingsConfiguration {

    REDIS_URI("redis-uri"),

    MYSQL_HOST("mysql.host"),
    MYSQL_PORT("mysql.port"),
    MYSQL_USERNAME("mysql.username"),
    MYSQL_PASSWORD("mysql.password"),
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
}

package tech.zmario.enhancedoitc.connector.enums;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public enum SettingsConfiguration {

    REDIS_URI("redis-uri"),
    ;

    private final String path;

    public String getString(Plugin plugin) {
        return plugin.getConfig().getString(path);
    }
}

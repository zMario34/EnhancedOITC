package tech.zmario.enhancedoitc.connector.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class Utils {

    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

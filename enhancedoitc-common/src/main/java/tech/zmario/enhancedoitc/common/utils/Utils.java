package tech.zmario.enhancedoitc.common.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@UtilityClass
public class Utils {

    public final Executor POOL_EXECUTOR = Executors.newFixedThreadPool(2);

    public String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public CompletableFuture<Void> sendPlayersToServer(Plugin plugin, String server, List<Player> players) {
        return CompletableFuture.runAsync(() -> {

            try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(byteOutput)) {
                out.writeUTF("Connect");
                out.writeUTF(server);

                for (Player player : players)
                    player.sendPluginMessage(plugin, "BungeeCord", byteOutput.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
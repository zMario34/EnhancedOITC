package tech.zmario.enhancedoitc.game.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.game.EnhancedOITC;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@UtilityClass
public class Utils {

    public final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private int currentGameId = 0;

    public final Executor POOL_EXECUTOR = Executors.newFixedThreadPool(2);

    public String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    // yeah, reflection is bad, but it's the only way to do it, I don't want to make all the wrappers for only a packet
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Class<?> PacketPlayOutTitleClass = getNMSClass("PacketPlayOutTitle");
            Class<?> IChatBaseComponentClass = getNMSClass("IChatBaseComponent");

            Constructor<?> constructor = PacketPlayOutTitleClass.getConstructor(PacketPlayOutTitleClass
                    .getDeclaredClasses()[0], IChatBaseComponentClass, int.class, int.class, int.class);

            Object titleComponent = IChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + title + "\"}");
            Object subTitleComponent = IChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + subtitle + "\"}");

            Object titlePacket = constructor.newInstance(PacketPlayOutTitleClass
                    .getDeclaredClasses()[0].getField("TITLE").get(null), titleComponent, fadeIn, stay, fadeOut);
            Object subTitlePacket = constructor.newInstance(PacketPlayOutTitleClass
                    .getDeclaredClasses()[0].getField("SUBTITLE")
                    .get(null), subTitleComponent, fadeIn, stay, fadeOut);

            sendPacket(player, titlePacket);
            sendPacket(player, subTitlePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find NMS class " + name, e);
        }
    }

    public void sendPacket(Player player, Object packet) {
        Object playerHandle;
        Object playerConnection;

        try {
            playerHandle = player.getClass().getMethod("getHandle").invoke(player);
            playerConnection = playerHandle.getClass().getField("playerConnection").get(playerHandle);

            if (VERSION.contains("18")) {
                playerConnection.getClass().getMethod("a", getNMSClass("Packet"))
                        .invoke(playerConnection, packet);
            } else {
                playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"))
                        .invoke(playerConnection, packet);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to send packet " + packet, e);
        }
    }

    public CompletableFuture<Void> sendPlayersToServer(EnhancedOITC plugin, String server, List<Player> players) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        try (DataOutputStream out = new DataOutputStream(byteOutput)) {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }

        for (Player player : players)
            player.sendPluginMessage(plugin, "BungeeCord", byteOutput.toByteArray());

        return CompletableFuture.completedFuture(null);
    }

    public int getNextGameId() {
        return currentGameId++;
    }
}

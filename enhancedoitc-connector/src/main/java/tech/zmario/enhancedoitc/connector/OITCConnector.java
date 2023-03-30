package tech.zmario.enhancedoitc.connector;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tech.zmario.enhancedoitc.common.handler.RedisHandler;
import tech.zmario.enhancedoitc.connector.arena.ArenaManager;
import tech.zmario.enhancedoitc.connector.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.connector.listeners.GameUpdateListener;

import java.io.File;

@Getter
public class OITCConnector extends JavaPlugin {

    private ArenaManager arenaManager;
    private RedisHandler redisHandler;

    private YamlConfiguration messages;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        getLogger().info("Enabling EnhancedOITC (Connector) v" + getDescription().getVersion() + "...");

        saveDefaultConfig();
        loadMessages();
        startInstances();

        getLogger().info("EnhancedOITC Connector enabled in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private void startInstances() {
        arenaManager = new ArenaManager();
        redisHandler = new RedisHandler(this, SettingsConfiguration.REDIS_URI.getString(this));

        registerRedisListeners();
    }

    private void registerRedisListeners() {
        redisHandler.subscribe("enhancedoitc:update", new GameUpdateListener(this));
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists())
            saveResource("messages.yml", false);

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
}


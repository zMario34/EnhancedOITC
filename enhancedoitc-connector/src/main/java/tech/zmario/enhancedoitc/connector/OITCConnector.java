package tech.zmario.enhancedoitc.connector;

import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tech.zmario.enhancedoitc.common.handler.RedisHandler;
import tech.zmario.enhancedoitc.connector.arena.ArenaManager;
import tech.zmario.enhancedoitc.connector.commands.OITCCommand;
import tech.zmario.enhancedoitc.connector.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.connector.listeners.ConnectionListener;
import tech.zmario.enhancedoitc.connector.listeners.redis.GameUpdateListener;
import tech.zmario.enhancedoitc.connector.sql.SQLManager;
import tech.zmario.enhancedoitc.connector.storage.LocalStorage;
import tech.zmario.enhancedoitc.connector.tasks.ScoreboardTask;

import java.io.File;


@Getter
public class OITCConnector extends JavaPlugin {

    private ArenaManager arenaManager;
    private RedisHandler redisHandler;
    private SQLManager sqlManager;
    private LocalStorage storage;

    private YamlConfiguration messages;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        saveDefaultConfig();
        loadMessages();
        startInstances();

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("EnhancedOITC Connector enabled in " + (System.currentTimeMillis() - start) + "ms!");
    }

    @Override
    public void onDisable() {
        sqlManager.onDisable();
    }

    private void startInstances() {
        sqlManager = new SQLManager(this);
        redisHandler = new RedisHandler(this, SettingsConfiguration.REDIS_URI.getString(this));
        arenaManager = new ArenaManager();
        storage = new LocalStorage();

        redisHandler.subscribe("enhancedoitc:update", new GameUpdateListener(this));

        CommandService drink = Drink.get(this);

        drink.register(new OITCCommand(this), "oitc", "oneinthechamber");
        drink.registerCommands();

        new ScoreboardTask(this).runTaskTimer(this, 0L, 40L);
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists())
            saveResource("messages.yml", false);

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigurations() {
        reloadConfig();
        loadMessages();
    }
}


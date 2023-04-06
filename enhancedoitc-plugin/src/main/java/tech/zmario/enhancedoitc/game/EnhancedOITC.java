package tech.zmario.enhancedoitc.game;

import com.grinderwolf.swm.api.SlimePlugin;
import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import tech.zmario.enhancedoitc.common.handler.RedisHandler;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameRequestPacket;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.commands.OITCCommand;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.game.listeners.bukkit.ConnectionListener;
import tech.zmario.enhancedoitc.game.listeners.bukkit.DamageListener;
import tech.zmario.enhancedoitc.game.listeners.bukkit.PlayerChatListener;
import tech.zmario.enhancedoitc.game.listeners.bukkit.WorldLoadListener;
import tech.zmario.enhancedoitc.game.listeners.redis.RedisConnectionListener;
import tech.zmario.enhancedoitc.game.manager.ArenaManager;
import tech.zmario.enhancedoitc.game.manager.SetupManager;
import tech.zmario.enhancedoitc.game.storage.LocalStorage;
import tech.zmario.enhancedoitc.game.tasks.ScoreboardTask;
import tech.zmario.enhancedoitc.game.workload.WorkloadThread;

import java.io.File;

@Getter
public class EnhancedOITC extends JavaPlugin {

    private SlimePlugin slimePlugin;
    private ArenaManager arenaManager;
    private SetupManager setupManager;
    private WorkloadThread workloadThread;
    private LocalStorage storage;
    private RedisHandler redisHandler;

    private YamlConfiguration messages;

    @Override
    public void onEnable() {
        getLogger().info("Enabling EnhancedOITC (Game server) v" + getDescription().getVersion() + "...");
        long start = System.currentTimeMillis();

        saveDefaultConfig();
        loadMessages();

        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer(ChatColor.RED + "Server reloading, please rejoin.");

        startInstances();

        registerListeners(new DamageListener(this), new ConnectionListener(this),
                new WorldLoadListener(this), new PlayerChatListener(this));

        loadArenas();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        new ScoreboardTask(this).runTaskTimer(this, 20L, 15L);
        getLogger().info("Enabled in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        arenaManager.disable();
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists())
            saveResource("messages.yml", false);

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void startInstances() {
        storage = new LocalStorage();
        arenaManager = new ArenaManager(this);
        slimePlugin = (SlimePlugin) getServer().getPluginManager().getPlugin("SlimeWorldManager");
        redisHandler = new RedisHandler(this, SettingsConfiguration.REDIS_URI.getString(this));
        setupManager = new SetupManager(this);

        workloadThread = new WorkloadThread();

        CommandService drink = Drink.get(this);

        drink.register(new OITCCommand(this), "oitc", "oneinthechamber");
        drink.registerCommands();

        workloadThread.register(this);
        redisHandler.subscribe("enhancedoitc:connection", new RedisConnectionListener(this));
        redisHandler.publish("enhancedoitc:connection", new GameRequestPacket());
    }

    private void loadArenas() {
        File folder = new File(getDataFolder(), "arenas");

        if (!folder.exists()) folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            new Arena(this, file.getName().replace(".yml", ""));
        }

        getLogger().info("Loaded " + arenaManager.getArenas().size() + " arenas!");
    }

    public void debug(String message) {
        if (!getConfig().getBoolean("debug")) return;
        getLogger().info(message);
    }

    public void reloadConfigurations() {
        reloadConfig();
        loadMessages();
    }
}

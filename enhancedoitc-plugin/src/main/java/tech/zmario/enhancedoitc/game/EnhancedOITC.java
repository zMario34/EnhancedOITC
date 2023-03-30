package tech.zmario.enhancedoitc.game;

import com.grinderwolf.swm.api.SlimePlugin;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tech.zmario.enhancedoitc.common.handler.RedisHandler;
import tech.zmario.enhancedoitc.game.manager.ArenaManager;
import tech.zmario.enhancedoitc.game.storage.LocalStorage;
import tech.zmario.enhancedoitc.game.workload.WorkloadThread;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;

import java.io.File;

@Getter
public class EnhancedOITC extends JavaPlugin {

    private SlimePlugin slimePlugin;
    private ArenaManager arenaManager;
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

        startInstances();

        getLogger().info("Enabled in " + (System.currentTimeMillis() - start) + "ms!");
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

        workloadThread = new WorkloadThread();

        workloadThread.register(this);
    }
}

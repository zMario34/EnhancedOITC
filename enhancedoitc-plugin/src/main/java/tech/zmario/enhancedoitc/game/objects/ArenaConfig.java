package tech.zmario.enhancedoitc.game.objects;

import lombok.Getter;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
public class ArenaConfig {

    private final Plugin plugin;
    private final String path;

    private final File file;

    private YamlConfiguration config;

    public ArenaConfig(Plugin plugin, String arenaName) {
        this.plugin = plugin;
        this.path = arenaName + ".yml";

        File folder = new File(plugin.getDataFolder(), "arenas");

        if (!folder.exists())
            folder.mkdirs();

        file = new File(folder, path);

        try {
            config = create();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Impossibile creare il file dell'arena: " + path);
        }

        Configuration config = getConfig();

        config.addDefault("max-players", 6);
        config.addDefault("spawn-protection", 3);

        config.options().copyDefaults(true);

        save();
    }

    private YamlConfiguration create() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void delete() {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to delete arena file: " + path);
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to save arena file: " + path);
        }
    }
}

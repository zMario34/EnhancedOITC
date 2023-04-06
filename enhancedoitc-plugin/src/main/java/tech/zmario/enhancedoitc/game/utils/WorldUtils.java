package tech.zmario.enhancedoitc.game.utils;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@UtilityClass
public class WorldUtils {


    public static SlimeWorld loadWorld(EnhancedOITC plugin, double[] location, String name, boolean readOnly) {
        SlimePlugin slime = plugin.getSlimePlugin();
        World world = Bukkit.getWorld(name);

        if (world != null) return null;

        SlimeLoader loader = slime.getLoader(SettingsConfiguration.LOADER_TYPE.getString(plugin));
        SlimePropertyMap spm = new SlimePropertyMap();

        spm.setString(SlimeProperties.WORLD_TYPE, "flat");
        spm.setString(SlimeProperties.DIFFICULTY, "normal");

        spm.setInt(SlimeProperties.SPAWN_X, (int) location[0]);
        spm.setInt(SlimeProperties.SPAWN_Y, (int) location[1]);
        spm.setInt(SlimeProperties.SPAWN_Z, (int) location[2]);

        spm.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
        spm.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);
        spm.setBoolean(SlimeProperties.PVP, true);

        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    SlimeWorld slimeWorld = (loader.worldExists(name)) ? slime.loadWorld(loader, name, readOnly, spm) :
                            slime.createEmptyWorld(loader, name, false, spm);

                    Bukkit.getScheduler().runTask(plugin, () -> slime.generateWorld(slimeWorld));

                    return slimeWorld;
                } catch (CorruptedWorldException | NewerFormatException | WorldInUseException |
                         WorldAlreadyExistsException | IOException | UnknownWorldException e) {
                    e.printStackTrace();
                    plugin.getLogger().severe("Error while loading world " + name + ": " + e.getMessage());
                }

                return null;
            }, Utils.POOL_EXECUTOR).get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error while loading world " + name + ": " + e.getMessage());
        }

        return null;
    }
}

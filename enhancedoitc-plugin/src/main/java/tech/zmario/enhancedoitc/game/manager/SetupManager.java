package tech.zmario.enhancedoitc.game.manager;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.game.objects.ArenaConfig;
import tech.zmario.enhancedoitc.game.objects.SetupArena;
import tech.zmario.enhancedoitc.game.utils.MessageBuilder;
import tech.zmario.enhancedoitc.game.utils.WorldUtils;
import tech.zmario.enhancedoitc.game.workload.impl.TeleportablePlayerWorkload;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class SetupManager {

    private final EnhancedOITC plugin;
    private final Map<UUID, SetupArena> setupPlayers = Maps.newHashMap();

    public void start(Player player, String arenaName) {
        ArenaConfig arenaConfig = new ArenaConfig(plugin, arenaName);
        SetupArena setupArena = new SetupArena(player, arenaName, arenaConfig);

        arenaConfig.getConfig().set("display-name", arenaName);

        setupPlayers.put(player.getUniqueId(), setupArena);

        player.getInventory().clear();

        CompletableFuture.runAsync(() -> {
            if (Bukkit.getWorld(arenaName) != null)
                Bukkit.unloadWorld(arenaName, false);
        }).whenComplete((v, t) -> WorldUtils.loadWorld(plugin, new double[]{0, 100, 0}, arenaName, false));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getWorkloadThread().addWorkload(new TeleportablePlayerWorkload(player, Bukkit.getWorld(arenaName).getSpawnLocation()));

            player.setAllowFlight(true);
            player.setFlying(true);

            player.setGameMode(GameMode.CREATIVE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE, 1, false, false));
        }, 8L);

        sendHelp(player);
    }

    public void cancel(Player player) {
        Optional<SetupArena> optionalArena = getArena(player);

        if (optionalArena.isEmpty()) return;
        SetupArena setupArena = optionalArena.get();

        player.getInventory().clear();

        player.setFlying(false);
        player.setAllowFlight(false);

        player.setGameMode(GameMode.ADVENTURE);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        Bukkit.unloadWorld(setupArena.getName(), false);

        setupPlayers.remove(player.getUniqueId());
    }

    public void save(Player player) {
        SetupArena setupArena = setupPlayers.get(player.getUniqueId());

        if (setupArena == null) return;
        setupArena.getArenaConfig().save();

        player.getInventory().clear();

        player.setFlying(false);
        player.setAllowFlight(false);

        player.setGameMode(GameMode.ADVENTURE);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        try {
            World world = Bukkit.getWorld(setupArena.getName());

            world.save();
            world.setAutoSave(false);

            Utils.sendPlayersToServer(plugin, SettingsConfiguration.LOBBY_SERVER.getString(plugin),
                    Collections.singletonList(player));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.unloadWorld(setupArena.getName(), true);
                new Arena(plugin, setupArena.getName());
            }, 5L);

            setupPlayers.remove(player.getUniqueId());

            player.sendMessage(Utils.colorize("&aArena saved and enabled successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(Utils.colorize("&cAn error occurred while saving the arena."));
        }
    }

    public Optional<SetupArena> getArena(Player player) {
        return Optional.ofNullable(setupPlayers.get(player.getUniqueId()));
    }

    public boolean isInSetup(Player player) {
        return setupPlayers.containsKey(player.getUniqueId());
    }

    public void sendHelp(Player player) {
        SetupArena arena = setupPlayers.get(player.getUniqueId());

        if (arena == null) return;
        FileConfiguration config = arena.getArenaConfig().getConfig();

        MessageBuilder builder = new MessageBuilder(Utils.colorize("&a&lSetup OITC Arena &8» &7" + arena.getName()));

        builder.append(new MessageBuilder("\n&8• &eAdd spawn point " +
                (config.get("spawn-locations") == null ? "&c(✖)" : "&a(" + config.getStringList("spawn-locations").size() + ")"))
                .runCommand("/oitc addspawn"));

        builder.append(new MessageBuilder("\n&8• &bSet waiting location " +
                (config.get("waiting-location") == null ? "&c(✖)" : "&a(✓)"))
                .runCommand("/oitc setwaitinglobby"));

        builder.append(new MessageBuilder("\n&8• &6Set arena display name "+
                (config.get("display-name") == null ? "&c(✖)" : "&a(" + config.getString("display-name") + ")"))
                .runCommand("/oitc setdisplayname"));

        builder.append(new MessageBuilder("\n&8• &cSet arena kit " +
                (config.get("kit-items") == null ? "&c(✖)" : "&a(✓)"))
                .runCommand("/oitc setkit"));

        builder.append(new MessageBuilder("\n&8• &dSet arena min players " +
                (config.get("min-players") == null ? "&c(✖)" : "&a(" + config.getInt("min-players") + ")"))
                .runCommand("/oitc setminplayers"));
        builder.append(new MessageBuilder("\n&8• &dSet arena max players " +
                (config.get("max-players") == null ? "&c(✖)" : "&a(" + config.getInt("max-players") + ")"))
                .runCommand("/oitc setmaxplayers"));

        builder.append(new MessageBuilder("\n&8» &aSave arena")
                .runCommand("/oitc save"));

        builder.send(player);
    }
}

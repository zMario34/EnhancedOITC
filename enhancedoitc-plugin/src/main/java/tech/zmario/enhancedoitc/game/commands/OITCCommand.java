package tech.zmario.enhancedoitc.game.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.enums.SetupAction;
import tech.zmario.enhancedoitc.game.objects.SetupArena;
import tech.zmario.enhancedoitc.game.utils.InventorySerializer;
import tech.zmario.enhancedoitc.game.utils.LocationUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class OITCCommand {

    private final EnhancedOITC plugin;

    @Command(name = "", desc = "Help command for OITC", aliases = {"help"})
    @Require("")
    public void root(@Sender Player player) {
        if (plugin.getSetupManager().isInSetup(player)) {
            plugin.getSetupManager().sendHelp(player);
            return;
        }

        if (!player.hasPermission("oitc.admin")) {
            MessagesConfiguration.COMMAND_HELP_USER.send(player, plugin);
        } else {
            MessagesConfiguration.COMMAND_HELP_ADMIN.send(player, plugin);
        }
    }

    @Command(name = "reload", desc = "Reloads the plugin configurations")
    @Require("oitc.admin")
    public void reload(@Sender Player player) {
        plugin.reloadConfigurations();
        MessagesConfiguration.COMMAND_RELOAD_SUCCESS.send(player, plugin);
    }

    @Command(name = "setup", desc = "Setup an arena")
    @Require("oitc.admin")
    public void setup(@Sender Player player, String arenaName) {
        plugin.getSetupManager().start(player, arenaName);
    }

    @Command(name = "cancel", desc = "Cancels the setup")
    @Require("oitc.admin")
    public void cancel(@Sender Player player) {
        plugin.getSetupManager().cancel(player);
    }

    @Command(name = "save", desc = "Saves the arena")
    @Require("oitc.admin")
    public void save(@Sender Player player) {
        plugin.getSetupManager().save(player);
    }

    @Command(name = "addspawn", desc = "Adds a spawn to the arena")
    @Require("oitc.admin")
    public void addSpawn(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        List<String> spawns = setupArena.getArenaConfig().getConfig().getStringList("spawn-locations");

        spawns.add(LocationUtils.serializeLocation(player.getLocation()));
        setupArena.getArenaConfig().getConfig().set("spawn-locations", spawns);

        player.sendMessage(Utils.colorize("&8» &aSpawn added successfully!"));
        plugin.getSetupManager().sendHelp(player);
    }

    @Command(name = "setwaitinglobby", desc = "Sets the waiting lobby")
    @Require("oitc.admin")
    public void setWaitingLobby(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        setupArena.getArenaConfig().getConfig().set("lobby-location", LocationUtils.serializeLocation(player.getLocation()));

        player.sendMessage(Utils.colorize("&8» &aLobby location set successfully!"));
        plugin.getSetupManager().sendHelp(player);
    }

    @Command(name = "setdisplayname", desc = "Sets the waiting lobby")
    @Require("oitc.admin")
    public void setDisplayName(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        setupArena.setCurrentAction(SetupAction.DISPLAY_NAME);
        player.sendMessage(Utils.colorize("&8» &aType the display name in chat:"));
    }

    @Command(name = "setminplayers", desc = "Sets the minimum players")
    @Require("oitc.admin")
    public void setMinimumPlayers(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        setupArena.setCurrentAction(SetupAction.MINIMUM_PLAYERS);
        player.sendMessage(Utils.colorize("&8» &aType the minimum players in chat:"));
    }

    @Command(name = "setmaxplayers", desc = "Sets the maximum players")
    @Require("oitc.admin")
    public void setMaximumPlayers(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        setupArena.setCurrentAction(SetupAction.MAXIMUM_PLAYERS);
        player.sendMessage(Utils.colorize("&8» &aType the maximum players in chat:"));
    }

    @Command(name = "setkit", desc = "Sets the kit")
    @Require("oitc.admin")
    public void setKit(@Sender Player player) {
        SetupArena setupArena = setupChecks(player);

        if (setupArena == null) return;
        setupArena.getArenaConfig().getConfig().set("kit-items", InventorySerializer.toBase64(player.getInventory().getContents()));
        setupArena.getArenaConfig().getConfig().set("kit-armor", InventorySerializer.toBase64(player.getInventory().getArmorContents()));

        player.sendMessage(Utils.colorize("&8» &aKit set successfully!"));
    }
    private SetupArena setupChecks(Player player) {
        if (plugin.getArenaManager().getArena(player.getUniqueId()).isPresent()) {
            MessagesConfiguration.COMMAND_SETUP_IN_ARENA.send(player, plugin);
            return null;
        }
        Optional<SetupArena> setupArenaOptional = plugin.getSetupManager().getArena(player);

        if (setupArenaOptional.isEmpty()) {
            MessagesConfiguration.COMMAND_SETUP_NOT_IN_SETUP.send(player, plugin);
            return null;
        }

        return setupArenaOptional.get();
    }

    @Command(name = "leave", desc = "Leaves the game")
    @Require("")
    public void leave(@Sender Player player) {
        Optional<Arena> arenaOptional = plugin.getArenaManager().getArena(player.getUniqueId());

        if (arenaOptional.isEmpty()) {
            MessagesConfiguration.COMMAND_LEAVE_NOT_IN_ARENA.send(player, plugin);
            return;
        }

        arenaOptional.get().removePlayer(player);
    }
}

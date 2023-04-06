package tech.zmario.enhancedoitc.connector.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.connector.OITCConnector;
import tech.zmario.enhancedoitc.connector.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.connector.objects.GameArena;

import java.util.Optional;

@RequiredArgsConstructor
public class OITCCommand {

    private final OITCConnector plugin;

    @Command(name = "", desc = "Help command for OITC", aliases = {"help"})
    @Require("")
    public void root(@Sender Player player) {
        MessagesConfiguration.COMMAND_HELP.send(player, plugin);
    }

    @Command(name = "join", desc = "Joins the game")
    @Require("")
    public void join(@Sender Player player, @OptArg String arenaName) {
        Optional<GameArena> arenaOptional;

        if (arenaName == null) {
            arenaOptional = plugin.getArenaManager().getRandomArena(null);
        } else {
            arenaOptional = plugin.getArenaManager().getRandomArena(arenaName);
        }

        if (arenaOptional.isEmpty()) {
            MessagesConfiguration.NO_ARENAS_AVAILABLE.send(player, plugin);
            return;
        }
        GameArena arena = arenaOptional.get();

        arena.addPlayer(player);
    }

    @Command(name = "reload", desc = "Reloads the plugin configurations")
    @Require("oitc.admin")
    public void reload(@Sender Player player) {
        plugin.reloadConfigurations();
        MessagesConfiguration.COMMAND_RELOAD_SUCCESS.send(player, plugin);
    }
}

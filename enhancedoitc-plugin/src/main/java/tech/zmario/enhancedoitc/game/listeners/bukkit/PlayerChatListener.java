package tech.zmario.enhancedoitc.game.listeners.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.enums.SetupAction;
import tech.zmario.enhancedoitc.game.objects.SetupArena;

import java.util.Optional;

@RequiredArgsConstructor
public class PlayerChatListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        Optional<SetupArena> setupArenaOptional = plugin.getSetupManager().getArena(player);

        if (setupArenaOptional.isPresent()) {
            SetupArena setupArena = setupArenaOptional.get();
            SetupAction action = setupArena.getCurrentAction();

            event.setCancelled(true);

            if (action == null) return;

            if (action == SetupAction.DISPLAY_NAME) {
                setupArena.getArenaConfig().getConfig().set("display-name", Utils.colorize(event.getMessage()));
                setupArena.setCurrentAction(null);

                player.sendMessage(Utils.colorize("&8» &cDisplay name set successfully!"));
                return;
            }
            int number;

            try {
                number = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.colorize("&8» &cYou must enter a number!"));
                return;
            }

            if (number < 1) {
                player.sendMessage(Utils.colorize("&8» &cYou must enter a number greater than 0!"));
                return;
            }

            switch (action) {
                case MINIMUM_PLAYERS:
                    if (number > setupArena.getArenaConfig().getConfig().getInt("max-players")) {
                        player.sendMessage(Utils.colorize("&8» &cMinimum players cannot be greater than maximum players!"));
                        return;
                    }

                    setupArena.getArenaConfig().getConfig().set("min-players", number);
                    player.sendMessage(Utils.colorize("&8» &cMinimum players set successfully!"));
                    break;
                case MAXIMUM_PLAYERS:
                    if (number < setupArena.getArenaConfig().getConfig().getInt("min-players")) {
                        player.sendMessage(Utils.colorize("&8» &cMaximum players cannot be less than minimum players!"));
                        return;
                    }

                    setupArena.getArenaConfig().getConfig().set("max-players", number);
                    player.sendMessage(Utils.colorize("&8» &cMaximum players set successfully!"));
                    break;
            }

            setupArena.setCurrentAction(null);
            return;
        }
        Optional<Arena> arenaOptional = plugin.getArenaManager().getArena(player.getUniqueId());

        if (arenaOptional.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        Arena arena = arenaOptional.get();
        String message = event.getMessage();
        String format = event.getFormat();

        switch (arena.getGameState()) {
            case STARTING:
            case WAITING:
                format = MessagesConfiguration.CHAT_FORMAT_WAITING.getString(plugin, player);
                break;
            case PLAYING:
                format = MessagesConfiguration.CHAT_FORMAT_PLAYING.getString(plugin, player);
                break;
            case RESTARTING:
                format = MessagesConfiguration.CHAT_FORMAT_ENDING.getString(plugin, player);
                break;
        }

        System.out.println(format);
        format = format.replace("%player%", player.getName())
                .replace("%message%", message)
                .replace("%", "%%");

        event.setFormat(format);
        event.setMessage(message);
        event.getRecipients().removeIf(recipient -> !arena.getPlayers().contains(recipient.getUniqueId()));
    }
}

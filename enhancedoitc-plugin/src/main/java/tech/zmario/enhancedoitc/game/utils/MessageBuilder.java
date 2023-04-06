package tech.zmario.enhancedoitc.game.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.common.utils.Utils;

public class MessageBuilder {

    private final TextComponent textComponent;

    public MessageBuilder(String text) {
        textComponent = new TextComponent(TextComponent.fromLegacyText(Utils.colorize(text)));
    }

    public MessageBuilder runCommand(String command) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public MessageBuilder suggestCommand(String command) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return this;
    }

    public MessageBuilder openUrl(String url) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    public MessageBuilder showText(String tooltip) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.colorize(tooltip))));
        return this;
    }

    public MessageBuilder append(String text) {
        textComponent.addExtra(text);
        return this;
    }

    public MessageBuilder append(MessageBuilder messageBuilder) {
        textComponent.addExtra(messageBuilder.toComponent());
        return this;
    }

    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(textComponent);
        }
    }

    public TextComponent toComponent() {
        return textComponent;
    }
}
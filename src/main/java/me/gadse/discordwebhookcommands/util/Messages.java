package me.gadse.discordwebhookcommands.util;

import me.gadse.discordwebhookcommands.DiscordWebhookCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public enum Messages {

    PREFIX(false),
    RELOAD,
    COOLDOWN,
    ERROR;

    private final boolean showPrefix;

    Messages() {
        this(true);
    }

    Messages(boolean showPrefix) {
        this.showPrefix = showPrefix;
    }

    private String message = ChatColor.RED + "ERROR LOADING MESSAGE FOR " + name();

    public void reloadMessage(DiscordWebhookCommands plugin) {
        message = plugin.color(plugin.getConfig().getString("messages." + name().toLowerCase()));
    }

    @SafeVarargs
    public final void sendMessage(CommandSender sender, Pair<RegexUtil, String>... placeholders) {
        String tempMessage = message;

        for (Pair<RegexUtil, String> placeholder : placeholders)
            tempMessage = placeholder.getKey().replaceAll(tempMessage, placeholder.getValue());

        if (tempMessage.isEmpty())
            return;

        sender.sendMessage((showPrefix ? PREFIX.message : "") + tempMessage);
    }
}

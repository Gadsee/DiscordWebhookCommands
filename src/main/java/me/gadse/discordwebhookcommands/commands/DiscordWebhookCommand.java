package me.gadse.discordwebhookcommands.commands;

import me.gadse.discordwebhookcommands.DiscordWebhookCommands;
import me.gadse.discordwebhookcommands.util.Messages;
import me.gadse.discordwebhookcommands.util.WebhookCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DiscordWebhookCommand implements CommandExecutor {

    private final DiscordWebhookCommands plugin;

    public DiscordWebhookCommand(DiscordWebhookCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        WebhookCommand webhookCommand = plugin.getCommandMap().get(label);
        if (webhookCommand == null) {
            if (args.length > 0
                    && args[0].equalsIgnoreCase("reload")
                    && sender.hasPermission("discordwebhookcommands.reload")) {
                plugin.reload();
                Messages.RELOAD.sendMessage(sender);
            }
            return true;
        }

        webhookCommand.acceptCommand(sender, args);
        return true;
    }
}

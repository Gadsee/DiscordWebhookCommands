package me.gadse.discordwebhookcommands.util;


import me.gadse.discordwebhookcommands.DiscordWebhookCommands;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class WebhookCommand {

    private final DiscordWebhookCommands plugin;
    private final String key, permission, permissionMessage, usage, response, webhookUrl, webhookAvatar, webhookName, embeds;
    private final long cooldown;
    private final int requiredArguments;
    private final boolean content;

    private final NamespacedKey namespacedKey;

    public WebhookCommand(DiscordWebhookCommands plugin, @NotNull String key,
                          @NotNull String permission, @NotNull String permissionMessage,
                          int requiredArguments, @NotNull String usage,
                          long cooldown,
                          @NotNull String response,
                          String webhookUrl, String webhookAvatar, String webhookName, String embeds, boolean content) {
        this.plugin = plugin;
        this.key = key;
        this.namespacedKey = new NamespacedKey(plugin, key);

        this.permission = permission;
        this.permissionMessage = permissionMessage;

        this.requiredArguments = requiredArguments;
        this.usage = usage;

        this.cooldown = cooldown;

        this.response = response;

        this.webhookUrl = webhookUrl;
        this.webhookAvatar = webhookAvatar;
        this.webhookName = webhookName;
        this.embeds = embeds;
        this.content = content;
    }

    public void acceptCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(permission)) {
            if (!permissionMessage.isEmpty())
                sender.sendMessage(permissionMessage);
            return;
        }

        if (args.length < requiredArguments) {
            if (!usage.isEmpty())
                sender.sendMessage(usage);
            return;
        }

        if (webhookUrl == null || webhookUrl.isEmpty())
            return;

        UUID uuid;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            uuid = player.getUniqueId();
            long coolDown = player.getPersistentDataContainer().getOrDefault(namespacedKey, PersistentDataType.LONG, 0L);
            if (coolDown > System.currentTimeMillis() && !player.hasPermission("discordwebhookcommands.bypass")) {
                Messages.COOLDOWN.sendMessage(player, new Pair<>(RegexUtil.COOLDOWN, millisecondsToString(coolDown)));
                return;
            }

            player.getPersistentDataContainer()
                    .set(namespacedKey, PersistentDataType.LONG, System.currentTimeMillis() + this.cooldown * 1000L);
        } else {
            uuid = new UUID(0, 0);
        }

        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
        if (webhookAvatar != null && !webhookAvatar.isEmpty()) {
            String tempAvatar = RegexUtil.PLAYER_NAME.replaceAll(webhookAvatar, sender.getName());
            tempAvatar = RegexUtil.PLAYER_UUID.replaceAll(tempAvatar, uuid.toString());

            webhook.setAvatarUrl(tempAvatar);
        }

        if (webhookName != null && !webhookName.isEmpty()) {
            String tempName = RegexUtil.PLAYER_NAME.replaceAll(webhookName, sender.getName());
            tempName = RegexUtil.PLAYER_UUID.replaceAll(tempName, uuid.toString());

            webhook.setUsername(tempName);
        }

        AtomicReference<String> content = new AtomicReference<>(String.join(" ", args));

        if (!sender.hasPermission("discordwebhookcommands.bypass"))
            plugin.getPatternSet().forEach(pattern -> content.set(pattern.matcher(content.get()).replaceAll("")));

        if (embeds != null) {
            webhook.setEmbeds(RegexUtil.CONTENT.replaceAll(
                    RegexUtil.PLAYER_UUID.replaceAll(
                            RegexUtil.PLAYER_NAME.replaceAll(embeds, sender.getName()),
                            uuid.toString()),
                    content.get()));

            if (!this.content)
                webhook.setContent(content.get());
        } else {
            webhook.setContent(content.get());
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                webhook.execute();
                if (!response.isEmpty())
                    sender.sendMessage(response);
            } catch (IOException | IllegalArgumentException e) {
                Messages.ERROR.sendMessage(sender, new Pair<>(RegexUtil.WEBHOOK, key));
                e.printStackTrace();
            }
        });
    }

    private String millisecondsToString(long time) {
        StringBuilder sb = new StringBuilder();
        long current = (time - System.currentTimeMillis()) / 1000;
        int days = (int) (current / 86400);
        current = current % 86400;
        int hours = (int) (current / 3600);
        current %= 3600;
        int minutes = (int) (current / 60);
        current %= 60;
        int seconds = (int) current;

        return sb.append(days > 0 ? days + "d " : "")
                .append(hours > 0 ? hours + "h " : "")
                .append(minutes > 0 ? minutes + "m " : "")
                .append(seconds).append("s").toString();
    }
}

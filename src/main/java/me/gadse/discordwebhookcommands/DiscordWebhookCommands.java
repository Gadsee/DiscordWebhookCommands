package me.gadse.discordwebhookcommands;

import me.gadse.discordwebhookcommands.commands.DiscordWebhookCommand;
import me.gadse.discordwebhookcommands.util.Messages;
import me.gadse.discordwebhookcommands.util.WebhookCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public final class DiscordWebhookCommands extends JavaPlugin {

    DiscordWebhookCommand discordWebhookCommand;
    private final Map<String, WebhookCommand> commandMap = new HashMap<>();
    private final Set<Pattern> patternSet = new HashSet<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs())
            return;
        saveDefaultConfig();

        discordWebhookCommand = new DiscordWebhookCommand(this);

        reload();
    }

    public void reload() {
        patternSet.clear();
        commandMap.clear();
        reloadConfig();

        for (Messages message : Messages.values())
            message.reloadMessage(this);

        ConfigurationSection commandSection = getConfig().getConfigurationSection("commands");
        if (commandSection == null) {
            getLogger().severe("You deleted the commands section in the config. Please don't do that.");
            return;
        }
        commandSection.getKeys(false).forEach(key -> {
            ConfigurationSection commandSettings = commandSection.getConfigurationSection(key + ".command-settings");
            if (commandSettings == null) {
                getLogger().warning("You didn't define any command settings at 'commands." + key + "'.");
                return;
            }

            String permission = commandSettings.getString("permission", "");
            if (permission == null)
                permission = "";

            String permissionMessage =
                    color(commandSettings.getString("permission-message", getServer().getPermissionMessage()));

            int requiredArguments = commandSettings.getInt("required-arguments", 0);

            String usage = color(commandSettings.getString("usage", ""));

            String response = color(commandSettings.getString("response"));

            ConfigurationSection webhookSettings = commandSection.getConfigurationSection(key + ".webhook-settings");
            if (webhookSettings == null) {
                getLogger().warning("You didn't define any webhook settings at 'commands." + key + "'.");
                return;
            }

            WebhookCommand webhookCommand = new WebhookCommand(this, key,
                    permission, permissionMessage,
                    requiredArguments, usage,
                    commandSettings.getLong("cooldown"),
                    response,
                    webhookSettings.getString("webhook-url"),
                    webhookSettings.getString("webhook-avatar"),
                    webhookSettings.getString("webhook-name"),
                    webhookSettings.getString("embeds"),
                    webhookSettings.getBoolean("content-in-embed-only"));
            commandMap.put(key, webhookCommand);
        });

        getConfig().getStringList("content-filter").forEach(filter -> patternSet.add(Pattern.compile(filter)));

        List<String> aliases = new ArrayList<>(commandMap.keySet());
        aliases.add("dwc");
        registerCommand("discordwebhookcommand", aliases, discordWebhookCommand);
    }

    @Override
    public void onDisable() {
        commandMap.clear();
    }

    public String color(String text) {
        if (text == null || text.isEmpty())
            return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Courtesy to https://www.spigotmc.org/threads/solved-modifying-a-registered-command-alias.375073/#post-3416210
    public void registerCommand(String command, List<String> aliases, CommandExecutor executor) {
        PluginCommand pluginCommand;
        Class<?> clazz = PluginCommand.class;

        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
            return;
        }

        try {
            pluginCommand = (PluginCommand) constructor.newInstance(command, this); // made the instance
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            ex.printStackTrace();
            return;
        }

        pluginCommand.setAliases(aliases); // Set aliases
        getServer().getCommandMap().register(command, pluginCommand); // Register on Bukkit's Map
        pluginCommand.register(getServer().getCommandMap()); // Register Map on your Command
        pluginCommand.setExecutor(executor); // Set executor
    }

    public Map<String, WebhookCommand> getCommandMap() {
        return commandMap;
    }

    public Set<Pattern> getPatternSet() {
        return patternSet;
    }
}

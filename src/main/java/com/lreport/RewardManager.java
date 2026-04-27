package com.lreport;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RewardManager {
    private final Main plugin;
    private final LanguageManager lang;
    private boolean enabled;
    private List<RewardCommand> rewardCommands;

    public RewardManager(Main plugin, LanguageManager lang) {
        this.plugin = plugin;
        this.lang = lang;
        this.rewardCommands = new ArrayList<>();
        loadSettings();
    }

    public void loadSettings() {
        this.enabled = plugin.getConfig().getBoolean("reward-enabled", false);
        this.rewardCommands.clear();

        if (!enabled) return;

        if (plugin.getConfig().contains("reward-commands")) {
            for (String key : plugin.getConfig().getConfigurationSection("reward-commands").getKeys(false)) {
                String command = plugin.getConfig().getString("reward-commands." + key + ".command", "");
                boolean console = plugin.getConfig().getBoolean("reward-commands." + key + ".console", true);

                if (!command.isEmpty()) {
                    rewardCommands.add(new RewardCommand(command, console));
                }
            }
        }

        if (enabled && rewardCommands.isEmpty()) {
            plugin.getLogger().warning("Reward system enabled but no reward-commands configured!");
        } else if (enabled) {
            plugin.getLogger().info("Loaded " + rewardCommands.size() + " reward commands.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void giveReward(Player reporter) {
        if (!isEnabled() || rewardCommands.isEmpty()) {
            return;
        }

        String playerName = reporter.getName();

        for (RewardCommand reward : rewardCommands) {
            String command = reward.getCommand().replace("%player%", playerName);

            try {
                if (reward.isConsole()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } else {
                    if (reporter.isOnline()) {
                        Bukkit.dispatchCommand(reporter, command);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to execute reward command: " + command + " - " + e.getMessage());
            }
        }

        sendRewardMessage(reporter);
    }

    private void sendRewardMessage(Player player) {
        if (!player.isOnline()) return;

        String title = lang.get("reward.received_title");
        String subtitle = lang.get("reward.received_subtitle");
        String message = lang.get("reward.received");

        if (title != null && !title.isEmpty()) {
            player.sendTitle(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', title),
                org.bukkit.ChatColor.translateAlternateColorCodes('&', subtitle),
                20, 60, 20
            );
        }

        if (message != null && !message.isEmpty()) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + message));
        }
    }

    public int getRewardCommandCount() {
        return rewardCommands.size();
    }

    public static class RewardCommand {
        private final String command;
        private final boolean console;

        public RewardCommand(String command, boolean console) {
            this.command = command;
            this.console = console;
        }

        public String getCommand() {
            return command;
        }

        public boolean isConsole() {
            return console;
        }
    }
}
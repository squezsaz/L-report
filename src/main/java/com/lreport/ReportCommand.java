package com.lreport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReportCommand implements CommandExecutor {
    private final Main plugin;
    private final LanguageManager lang;
    private final ReportManager reportManager;
    private final GUI gui;

    public ReportCommand(Main plugin, LanguageManager lang, ReportManager reportManager, GUI gui) {
        this.plugin = plugin;
        this.lang = lang;
        this.reportManager = reportManager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("gui.player_only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lreport.player") && !player.hasPermission("lreport.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.no_permission")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("lreport.admin")) {
                plugin.reloadConfig();
                lang.reload();
                plugin.reloadWebhookSettings();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.reload")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.no_permission")));
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("lang")) {
            if (player.hasPermission("lreport.admin")) {
                if (args.length > 1) {
                    String newLang = args[1];
                    if (newLang.equals("tr") || newLang.equals("en")) {
                        plugin.getConfig().set("language", newLang);
                        plugin.saveConfig();
                        lang.setLanguage(newLang);
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%language%", newLang);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.language_set", placeholders)));
                    }
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.no_permission")));
            }
            return true;
        }

        if (args.length > 0 && args.length >= 2 && args[0].equalsIgnoreCase("offline")) {
            if (!player.hasPermission("lreport.admin")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.no_permission")));
                return true;
            }
            String targetName = args[1];
            boolean allowOffline = plugin.getConfig().getBoolean("allow-offline-report", true);
            if (!allowOffline) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.offline_disabled")));
                return true;
            }
            Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                gui.openReasonSelectWithPlayer(player, targetName);
                return true;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", targetName);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.offline_report_success", placeholders)));
            gui.openReasonSelectWithPlayer(player, targetName);
            return true;
        }

        if (args.length > 0) {
            String targetName = args[0];
            if (targetName.equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.yourself")));
                return true;
            }
            boolean allowOffline = plugin.getConfig().getBoolean("allow-offline-report", true);
            Player target = Bukkit.getPlayer(targetName);
            if (target == null || !target.isOnline()) {
                if (!allowOffline) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.offline_disabled")));
                    return true;
                }
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", targetName);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.offline_report_success", placeholders)));
            }
            gui.openReasonSelectWithPlayer(player, targetName);
            return true;
        }

        gui.openMainMenu(player);
        return true;
    }
}
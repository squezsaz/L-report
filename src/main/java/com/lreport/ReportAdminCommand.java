package com.lreport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReportAdminCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final GUI gui;

    public ReportAdminCommand(Main plugin, GUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("gui.player_only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lreport.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.no_permission")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("search")) {
            if (args.length > 1) {
                String playerName = args[1];
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.searching_reports", 
                    java.util.Collections.singletonMap("%player%", playerName))));
                gui.openAdminMenuWithFilter(player, playerName);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getPrefix() + lang.get("gui.search_usage")));
            }
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
            gui.openStatsMenu(player);
            return true;
        }

        gui.openAdminMenu(player, null, "PENDING", null);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("search");
            completions.add("stats");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("search")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(onlinePlayer.getName());
                }
            }
        }
        
        return completions;
    }
}
package com.lreport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputListener implements Listener {
    private final Main plugin;
    private final LanguageManager lang;
    private final GUI gui;
    private final ReportManager reportManager;
    private final Map<UUID, String> awaitingSearch = new HashMap<>();
    private final Map<UUID, String> awaitingEvidence = new HashMap<>();
    private final Map<UUID, String> awaitingAdminSearch = new HashMap<>();

    public ChatInputListener(Main plugin, LanguageManager lang, GUI gui) {
        this.plugin = plugin;
        this.lang = lang;
        this.gui = gui;
        this.reportManager = plugin.getReportManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        
        if (awaitingSearch.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            String playerName = message;
            
            Player target = Bukkit.getPlayer(playerName);
            if (target != null && !target.getName().equals(player.getName())) {
                if (reportManager.hasPendingReport(player.getName(), target.getName())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        lang.getPrefix() + lang.get("gui.already_pending")));
                    openSearchMenuSync(player);
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        lang.getPrefix() + lang.get("gui.player_found", 
                            java.util.Collections.singletonMap("%player%", target.getName()))));
                    gui.openReasonSelectWithPlayer(player, target.getName());
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    lang.getPrefix() + lang.get("gui.player_not_online")));
                openSearchMenuSync(player);
            }
            awaitingSearch.remove(player.getUniqueId());
            return;
        }
        
        if (awaitingEvidence.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            String evidence = message;
            gui.setEvidenceFromChat(player, evidence);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                lang.getPrefix() + lang.get("gui.evidence_added")));
            awaitingEvidence.remove(player.getUniqueId());
            return;
        }
        
        if (awaitingAdminSearch.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            String searchName = message;
            gui.openAdminMenuWithFilter(player, searchName);
            awaitingAdminSearch.remove(player.getUniqueId());
        }
    }
    
    private void openSearchMenuSync(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> gui.openSearchMenu(player));
    }

    public void setAwaitingSearch(Player player) {
        awaitingSearch.put(player.getUniqueId(), "search");
        cancelAwaitingEvidence(player);
    }

    public void setAwaitingEvidence(Player player) {
        awaitingEvidence.put(player.getUniqueId(), "evidence");
        cancelAwaitingSearch(player);
        cancelAwaitingAdminSearch(player);
    }

    public void setAwaitingAdminSearch(Player player) {
        awaitingAdminSearch.put(player.getUniqueId(), "adminSearch");
        cancelAwaitingSearch(player);
        cancelAwaitingEvidence(player);
    }

    public void cancelAwaitingSearch(Player player) {
        awaitingSearch.remove(player.getUniqueId());
    }

    public void cancelAwaitingEvidence(Player player) {
        awaitingEvidence.remove(player.getUniqueId());
    }

    public void cancelAwaitingAdminSearch(Player player) {
        awaitingAdminSearch.remove(player.getUniqueId());
    }

    public boolean isAwaitingSearch(Player player) {
        return awaitingSearch.containsKey(player.getUniqueId());
    }

    public boolean isAwaitingEvidence(Player player) {
        return awaitingEvidence.containsKey(player.getUniqueId());
    }
}
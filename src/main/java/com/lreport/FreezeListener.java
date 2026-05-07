package com.lreport;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FreezeListener implements Listener {
    private final Main plugin;

    public FreezeListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        
        if (plugin.isFrozen(player)) {
            Location from = e.getFrom();
            Location to = e.getTo();
            
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                e.setTo(from);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        
        if (plugin.isFrozen(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.isFrozen(player) && player.isOnline()) {
                        player.teleport(e.getRespawnLocation());
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
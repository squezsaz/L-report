package com.lreport;

import com.lreport.api.LReportAPI;
import com.lreport.api.LReportAPIImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Main extends JavaPlugin {
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private ReportManager reportManager;
    private WebhookManager webhookManager;
    private GUI gui;
    private ChatInputListener chatInputListener;
    private RewardManager rewardManager;
    private Set<UUID> frozenPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Menajerlerin başlatılması
        languageManager = new LanguageManager(this);
        databaseManager = new DatabaseManager(this);
        reportManager = new ReportManager(this, databaseManager);
        webhookManager = new WebhookManager(this, languageManager);
        rewardManager = new RewardManager(this, languageManager);
        gui = new GUI(this, languageManager, reportManager);
        chatInputListener = new ChatInputListener(this, languageManager, gui);

        // API kaydı
        Bukkit.getServicesManager().register(LReportAPI.class, new LReportAPIImpl(this), this, ServicePriority.Normal);
        
        gui.setChatInputListener(chatInputListener);
        gui.setWebhookManager(webhookManager);
        gui.setRewardManager(rewardManager);

        // --- PLACEHOLDERAPI ENTEGRASYONU ---
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LReportExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion başarıyla kaydedildi!");
        } else {
            getLogger().warning("PlaceholderAPI bulunamadı, değişkenler (placeholders) aktif olmayacak.");
        }
        // -----------------------------------

        // Komutlar
        getCommand("rapor").setExecutor(new ReportCommand(this, languageManager, reportManager, gui));
        ReportAdminCommand adminCmd = new ReportAdminCommand(this, gui);
        getCommand("raporadmin").setExecutor(adminCmd);
        getCommand("raporadmin").setTabCompleter(adminCmd);
        
        // Dinleyiciler
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        
        getLogger().info("L-report plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (reportManager != null) {
            reportManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("L-report plugin disabled!");
    }

    public static LReportAPI getAPI() {
        RegisteredServiceProvider<LReportAPI> provider = Bukkit.getServicesManager().getRegistration(LReportAPI.class);
        return provider != null ? provider.getProvider() : null;
    }

    // Getter Metodları
    public LanguageManager getLanguageManager() { return languageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ReportManager getReportManager() { return reportManager; }
    public Set<UUID> getFrozenPlayers() { return frozenPlayers; }
    public WebhookManager getWebhookManager() { return webhookManager; }
    public RewardManager getRewardManager() { return rewardManager; }

    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public void reloadWebhookSettings() {
        if (webhookManager != null) {
            webhookManager.loadSettings();
        }
        if (rewardManager != null) {
            rewardManager.loadSettings();
        }
    }

    public void freezePlayer(Player player) {
        frozenPlayers.add(player.getUniqueId());
    }

    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
    }
}
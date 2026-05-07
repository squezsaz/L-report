package com.lreport;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final Main plugin;
    private YamlConfiguration langConfig;
    private String currentLang;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();

    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    private void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File trFile = new File(langFolder, "tr.yml");
        File enFile = new File(langFolder, "en.yml");
        
        if (!trFile.exists()) {
            plugin.saveResource("lang/tr.yml", false);
        }
        if (!enFile.exists()) {
            plugin.saveResource("lang/en.yml", false);
        }

        languages.put("tr", YamlConfiguration.loadConfiguration(trFile));
        languages.put("en", YamlConfiguration.loadConfiguration(enFile));

        String defaultLang = plugin.getConfig().getString("language", "tr");
        setLanguage(defaultLang);
    }

    public void setLanguage(String lang) {
        this.currentLang = lang;
        this.langConfig = languages.get(lang);
        if (langConfig == null) {
            this.langConfig = languages.get("tr");
            this.currentLang = "tr";
        }
    }

    public String get(String key) {
        return langConfig.getString(key, key);
    }

    public String get(String key, Map<String, String> placeholders) {
        String result = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&8[&cRapor&8] &7");
    }

    public void reload() {
        languages.clear();
        loadLanguages();
    }
}
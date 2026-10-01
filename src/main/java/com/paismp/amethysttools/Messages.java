package com.paismp.amethysttools;

import java.io.File;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages {
    private final JavaPlugin plugin;
    private final YamlConfiguration en;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.en = this.loadFromDisk("messages/messages_en.yml");
    }

    private YamlConfiguration loadFromDisk(String relativePath) {
        File file = new File(this.plugin.getDataFolder(), relativePath);
        return YamlConfiguration.loadConfiguration((File)file);
    }

    private YamlConfiguration pick(Player p) {
        String lang = this.plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
        if (lang.startsWith("en")) {
            return this.en;
        }
        return this.en;
    }

    public String get(Player p, String key) {
        YamlConfiguration cfg = this.pick(p);
        String prefix = this.color(cfg.getString("prefix", ""));
        String raw = cfg.getString(key, key);
        raw = raw.replace("{prefix}", prefix);
        return this.color(raw);
    }

    public String raw(Player p, String key) {
        YamlConfiguration cfg = this.pick(p);
        String raw = cfg.getString(key, key);
        if (key.endsWith("-lore-expire") && raw.contains("{date}")) {
            raw = raw.replace("Expires:", "Expires in:").replace("{date}", "{time}");
        }
        return this.color(raw);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)s);
    }
}

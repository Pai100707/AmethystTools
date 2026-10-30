package com.paismp.amethysttools;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AmethystToolsPlugin
extends JavaPlugin {
    private Messages messages;
    private NamespacedKey pickaxeKey;
    private NamespacedKey axeKey;
    private NamespacedKey shovelKey;
    private NamespacedKey hoeKey;
    private NamespacedKey expiresKey;
    private BukkitTask expirationLoreTask;

    public void onEnable() {
        if (this.getResource("config.yml") != null) {
            this.saveDefaultConfig();
        } else {
            this.getLogger().severe("config.yml not found in plugin jar! Check plugin.yml");
        }
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
        if (this.getResource("messages/messages_en.yml") != null) {
            this.saveResource("messages/messages_en.yml", false);
        }
        this.pickaxeKey = new NamespacedKey((Plugin)this, "amethyst_pickaxe");
        this.axeKey = new NamespacedKey((Plugin)this, "amethyst_axe");
        this.shovelKey = new NamespacedKey((Plugin)this, "amethyst_shovel");
        this.hoeKey = new NamespacedKey((Plugin)this, "amethyst_hoe");
        this.expiresKey = new NamespacedKey((Plugin)this, "expires_at");
        this.messages = new Messages(this);
        PluginCommand cmd = this.getCommand("amethysttools");
        if (cmd != null) {
            cmd.setExecutor((CommandExecutor)new MainCommand(this));
            cmd.setTabCompleter((TabCompleter)new MainTabCompleter(this));
        } else {
            this.getLogger().severe("Command 'amethysttools' not found! Check plugin.yml");
        }
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)new NoDurabilityListener(this), (Plugin)this);
        pm.registerEvents((Listener)new AmethystPickaxeListener(this), (Plugin)this);
        pm.registerEvents((Listener)new AmethystAxeListener(this), (Plugin)this);
        pm.registerEvents((Listener)new AmethystShovelListener(this), (Plugin)this);
        pm.registerEvents((Listener)new AmethystHoeListener(this), (Plugin)this);
        this.restartExpirationLoreUpdater();
        this.getLogger().info("AmethystTools enabled.");
    }

    public void restartExpirationLoreUpdater() {
        if (this.expirationLoreTask != null) {
            this.expirationLoreTask.cancel();
        }
        long intervalTicks = Math.max(1L, this.getConfig().getLong("update-time", 60L)) * 20L;
        this.expirationLoreTask = this.getServer().getScheduler()
            .runTaskTimer(this, new ExpirationLoreUpdater(this), intervalTicks, intervalTicks);
    }

    public void reloadMessages() {
        this.messages = new Messages(this);
    }

    public Messages messages() {
        return this.messages;
    }

    public NamespacedKey pickaxeKey() {
        return this.pickaxeKey;
    }

    public NamespacedKey axeKey() {
        return this.axeKey;
    }

    public NamespacedKey shovelKey() {
        return this.shovelKey;
    }

    public NamespacedKey hoeKey() {
        return this.hoeKey;
    }

    public NamespacedKey expiresKey() {
        return this.expiresKey;
    }
}

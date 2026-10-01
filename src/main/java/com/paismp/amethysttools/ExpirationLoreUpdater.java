package com.paismp.amethysttools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

final class ExpirationLoreUpdater implements Runnable {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private final AmethystToolsPlugin plugin;

    ExpirationLoreUpdater(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            PlayerInventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null || !item.hasItemMeta()) {
                    continue;
                }
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer data = meta.getPersistentDataContainer();
                Long expiresAt = data.get(this.plugin.expiresKey(), PersistentDataType.LONG);
                String messageKey = this.expirationMessageKey(data);
                if (expiresAt == null || messageKey == null) {
                    continue;
                }

                String format = this.plugin.getConfig().getString("time-format", "dd:HH:mm:ss");
                String remaining = formatRemaining(expiresAt, format);
                String message = this.plugin.messages().raw(player, messageKey).replace("{time}", remaining);
                Component expirationLine = LEGACY.deserialize(message);
                List<Component> lore = meta.lore();
                ArrayList<Component> updatedLore = lore == null ? new ArrayList<>() : new ArrayList<>(lore);
                int lastLine = updatedLore.size() - 1;
                if (lastLine >= 0 && updatedLore.get(lastLine).equals(expirationLine)) {
                    continue;
                }
                if (lastLine < 0) {
                    updatedLore.add(expirationLine);
                } else {
                    updatedLore.set(lastLine, expirationLine);
                }
                meta.lore(updatedLore);
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
        }
    }

    static String formatRemaining(long expiresAt, String format) {
        long secondsRemaining = Math.max(0L, (expiresAt - System.currentTimeMillis() + 999L) / 1000L);
        long days = secondsRemaining / 86400L;
        int hours = (int)((secondsRemaining % 86400L) / 3600L);
        int minutes = (int)((secondsRemaining % 3600L) / 60L);
        int seconds = (int)(secondsRemaining % 60L);
        String configuredFormat = format == null ? "dd:HH:mm:ss" : format;
        if (configuredFormat.equals("HH:mm:ss")) {
            configuredFormat = "dd:" + configuredFormat;
        }
        return configuredFormat
            .replace("dd", String.format(Locale.ROOT, "%02d", days))
            .replace("HH", String.format(Locale.ROOT, "%02d", hours))
            .replace("hh", String.format(Locale.ROOT, "%02d", hours))
            .replace("mm", String.format(Locale.ROOT, "%02d", minutes))
            .replace("ss", String.format(Locale.ROOT, "%02d", seconds));
    }

    private String expirationMessageKey(PersistentDataContainer data) {
        if (data.has(this.plugin.pickaxeKey(), PersistentDataType.BYTE)) {
            return "item-amethyst-pickaxe-lore-expire";
        }
        if (data.has(this.plugin.axeKey(), PersistentDataType.BYTE)) {
            return "item-amethyst-axe-lore-expire";
        }
        if (data.has(this.plugin.shovelKey(), PersistentDataType.BYTE)) {
            return "item-amethyst-shovel-lore-expire";
        }
        if (data.has(this.plugin.hoeKey(), PersistentDataType.BYTE)) {
            return "item-amethyst-hoe-lore-expire";
        }
        return null;
    }
}
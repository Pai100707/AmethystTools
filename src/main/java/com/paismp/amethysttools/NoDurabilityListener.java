package com.paismp.amethysttools;

import com.paismp.amethysttools.AmethystToolsPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NoDurabilityListener
implements Listener {
    private final AmethystToolsPlugin plugin;

    public NoDurabilityListener(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true)
    public void onDamage(PlayerItemDamageEvent e) {
        boolean isOurTool;
        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }
        Material type = item.getType();
        if (type != Material.NETHERITE_PICKAXE && type != Material.NETHERITE_AXE && type != Material.NETHERITE_SHOVEL && type != Material.NETHERITE_HOE) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        Byte pickVal = (Byte)meta.getPersistentDataContainer().get(this.plugin.pickaxeKey(), PersistentDataType.BYTE);
        Byte axeVal = (Byte)meta.getPersistentDataContainer().get(this.plugin.axeKey(), PersistentDataType.BYTE);
        Byte shovelVal = (Byte)meta.getPersistentDataContainer().get(this.plugin.shovelKey(), PersistentDataType.BYTE);
        Byte hoeVal = (Byte)meta.getPersistentDataContainer().get(this.plugin.hoeKey(), PersistentDataType.BYTE);
        boolean bl = isOurTool = pickVal != null && pickVal == 1 || axeVal != null && axeVal == 1 || shovelVal != null && shovelVal == 1 || hoeVal != null && hoeVal == 1;
        if (!isOurTool) {
            return;
        }
        e.setCancelled(true);
    }
}

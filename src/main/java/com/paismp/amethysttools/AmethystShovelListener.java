package com.paismp.amethysttools;

import com.paismp.amethysttools.AmethystToolsPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AmethystShovelListener
implements Listener {
    private final AmethystToolsPlugin plugin;
    private final NamespacedKey shovelKey;
    private final NamespacedKey expiresKey;
    private final Set<UUID> inProgress = new HashSet<UUID>();

    public AmethystShovelListener(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
        this.shovelKey = new NamespacedKey((Plugin)plugin, "amethyst_shovel");
        this.expiresKey = new NamespacedKey((Plugin)plugin, "expires_at");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler(ignoreCancelled=true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!this.isAmethystShovel(tool)) {
            return;
        }
        if (this.isExpired(tool)) {
            p.getInventory().setItemInMainHand(null);
            return;
        }
        Block center = e.getBlock();
        if (!Tag.MINEABLE_SHOVEL.isTagged(center.getType())) {
            return;
        }
        if (!this.inProgress.add(p.getUniqueId())) {
            return;
        }
        try {
            AmethystEffects.playSound(center);
            AmethystEffects.spawnParticles(center);
            int x = center.getX();
            int y = center.getY();
            int z = center.getZ();
            BlockFace face = this.yawToFace(p.getLocation().getYaw());
            boolean alongX = face == BlockFace.EAST || face == BlockFace.WEST;
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    if (a == 0 && b == 0) continue;
                    Block target = alongX
                        ? center.getWorld().getBlockAt(x, y + a, z + b)
                        : center.getWorld().getBlockAt(x + b, y + a, z);
                    if (target.getType() == Material.AIR || !Tag.MINEABLE_SHOVEL.isTagged(target.getType())) continue;
                    AmethystEffects.spawnParticles(target);
                    p.breakBlock(target);
                }
            }
        }
        finally {
            this.inProgress.remove(p.getUniqueId());
        }
    }

    private boolean isAmethystShovel(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte val = (Byte)meta.getPersistentDataContainer().get(this.shovelKey, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    private boolean isExpired(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return true;
        }
        Long expiresAt = (Long)meta.getPersistentDataContainer().get(this.expiresKey, PersistentDataType.LONG);
        if (expiresAt == null) {
            return true;
        }
        return System.currentTimeMillis() > expiresAt;
    }

    private BlockFace yawToFace(float yaw) {
        float y = (yaw % 360.0f + 360.0f) % 360.0f;
        if (y >= 45.0f && y < 135.0f) {
            return BlockFace.WEST;
        }
        if (y >= 135.0f && y < 225.0f) {
            return BlockFace.NORTH;
        }
        if (y >= 225.0f && y < 315.0f) {
            return BlockFace.EAST;
        }
        return BlockFace.SOUTH;
    }
}

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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;


public class AmethystPickaxeListener
implements Listener {
    private final AmethystToolsPlugin plugin;
    private final Set<UUID> inProgress = new HashSet<UUID>();
    private final NamespacedKey expiresKey;

    public AmethystPickaxeListener(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
        this.expiresKey = plugin.expiresKey();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler(ignoreCancelled=true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;

        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!this.isAmethystPickaxe(tool)) return;

        if (this.isExpired(tool)) {
            p.getInventory().setItemInMainHand(null);
            return;
        }

        Block center = e.getBlock();
        if (!Tag.MINEABLE_PICKAXE.isTagged(center.getType())) return;
        if (!this.inProgress.add(p.getUniqueId())) return;

        try {
            BlockFace face = this.yawToFace(p.getLocation().getYaw());
            boolean alongX = (face == BlockFace.EAST || face == BlockFace.WEST);

            AmethystEffects.playSound(center);
            AmethystEffects.spawnParticles(center);

            for (int a = -1; a <= 1; ++a) {       // แกน Y (สูง)
                for (int b = -1; b <= 1; ++b) {   // แกนกว้างของระนาบ
                    if (a == 0 && b == 0) continue;

                    Block target = alongX
                            ? center.getWorld().getBlockAt(center.getX(), center.getY() + a, center.getZ() + b)  // 1x3x3
                            : center.getWorld().getBlockAt(center.getX() + b, center.getY() + a, center.getZ()); // 3x3x1

                    Material type = target.getType();
                    if (type == Material.AIR || type == Material.BEDROCK || !Tag.MINEABLE_PICKAXE.isTagged(type)) continue;
                    AmethystEffects.spawnParticles(target);
                    p.breakBlock(target);
                }
            }
        } finally {
            this.inProgress.remove(p.getUniqueId());
        }
    }

    private boolean isAmethystPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte val = (Byte)meta.getPersistentDataContainer().get(this.plugin.pickaxeKey(), PersistentDataType.BYTE);
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

package com.paismp.amethysttools;

import com.paismp.amethysttools.AmethystToolsPlugin;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AmethystHoeListener
implements Listener {
    private final AmethystToolsPlugin plugin;
    private final NamespacedKey hoeKey;
    private final NamespacedKey expiresKey;

    public AmethystHoeListener(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
        this.hoeKey = new NamespacedKey((Plugin)plugin, "amethyst_hoe");
        this.expiresKey = new NamespacedKey((Plugin)plugin, "expires_at");
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clicked = e.getClickedBlock();
        if (clicked == null) {
            return;
        }
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!this.isAmethystHoe(tool)) {
            return;
        }
        if (this.isExpired(tool)) {
            p.getInventory().setItemInMainHand(null);
            return;
        }
        Material cropType = clicked.getType();
        Material seedType = this.seedFor(cropType);
        if (seedType == null) {
            return;
        }
        if (!(clicked.getBlockData() instanceof Ageable)) {
            return;
        }
        int cx = clicked.getX();
        int cy = clicked.getY();
        int cz = clicked.getZ();
        boolean didSomething = false;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                Ageable age;
                BlockData blockData;
                Block b = clicked.getWorld().getBlockAt(cx + dx, cy, cz + dz);
                if (b.getType() != cropType || !((blockData = b.getBlockData()) instanceof Ageable) || (age = (Ageable)blockData).getAge() < age.getMaximumAge()) continue;
                Collection drops = b.getDrops(tool, (Entity)p);
                Ageable replanted = (Ageable)b.getBlockData();
                replanted.setAge(0);
                b.setBlockData((BlockData)replanted, false);
                this.dropAllMinusOneSeed(b, drops, seedType);
                AmethystEffects.spawnParticles(b);
                didSomething = true;
            }
        }
        if (didSomething) {
            AmethystEffects.playSound(clicked);
            e.setCancelled(true);
        }
    }

    private boolean isAmethystHoe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_HOE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte val = (Byte)meta.getPersistentDataContainer().get(this.hoeKey, PersistentDataType.BYTE);
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

    private Material seedFor(Material crop) {
        return switch (crop) {
            case Material.WHEAT -> Material.WHEAT_SEEDS;
            case Material.CARROTS -> Material.CARROT;
            case Material.POTATOES -> Material.POTATO;
            case Material.BEETROOTS -> Material.BEETROOT_SEEDS;
            case Material.NETHER_WART -> Material.NETHER_WART;
            case Material.SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            default -> null;
        };
    }

    private void dropAllMinusOneSeed(Block where, Collection<ItemStack> drops, Material seedType) {
        boolean removedSeed = false;
        for (ItemStack it : drops) {
            if (!removedSeed && it != null && it.getType() == seedType) {
                int amt = it.getAmount();
                if (amt <= 1) {
                    removedSeed = true;
                    continue;
                }
                it = it.clone();
                it.setAmount(amt - 1);
                removedSeed = true;
            }
            if (it == null || it.getAmount() <= 0) continue;
            where.getWorld().dropItemNaturally(where.getLocation().add(0.5, 0.5, 0.5), it);
        }
    }
}

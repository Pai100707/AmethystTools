package com.paismp.amethysttools;

import com.paismp.amethysttools.AmethystToolsPlugin;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AmethystAxeListener
implements Listener {
    private final AmethystToolsPlugin plugin;
    private final NamespacedKey axeKey;
    private final NamespacedKey expiresKey;
    private final Set<UUID> inProgress = new HashSet<UUID>();

    public AmethystAxeListener(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
        this.axeKey = new NamespacedKey((Plugin)plugin, "amethyst_axe");
        this.expiresKey = new NamespacedKey((Plugin)plugin, "expires_at");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler(ignoreCancelled=true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!this.isAmethystAxe(tool)) {
            return;
        }
        if (this.isExpired(tool)) {
            p.getInventory().setItemInMainHand(null);
            return;
        }
        Block start = e.getBlock();
        if (!Tag.LOGS.isTagged(start.getType())) {
            return;
        }
        if (!this.inProgress.add(p.getUniqueId())) {
            return;
        }
        try {
            AmethystEffects.playSound(start);
            AmethystEffects.spawnParticles(start);
            int blockLimit = Math.max(1, this.plugin.getConfig().getInt("axe-block-limit", 8192));
            List<Block> treeBlocks = this.collectConnectedTreeBlocks(start, blockLimit);
            for (Block b : treeBlocks) {
                if (b.equals(start)) continue;
                AmethystEffects.spawnParticles(b);
                p.breakBlock(b);
            }
        }
        finally {
            this.inProgress.remove(p.getUniqueId());
        }
    }

    private boolean isAmethystAxe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte val = (Byte)meta.getPersistentDataContainer().get(this.axeKey, PersistentDataType.BYTE);
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

    private List<Block> collectConnectedTreeBlocks(Block start, int limit) {
        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        ArrayDeque<Block> q = new ArrayDeque<Block>();
        Set<String> visited = new HashSet<String>();
        ArrayList<Block> found = new ArrayList<Block>();
        q.add(start);
        while (!q.isEmpty() && found.size() < limit) {
            Block b = q.poll();
            if (!visited.add(this.blockKey(b)) || !Tag.LOGS.isTagged(b.getType())) continue;
            found.add(b);
            for (BlockFace face : faces) {
                Block neighbor = b.getRelative(face);
                if (Tag.LOGS.isTagged(neighbor.getType())) {
                    q.add(neighbor);
                }
            }
        }

        ArrayDeque<LeafNode> leafQueue = new ArrayDeque<LeafNode>();
        Set<String> visitedLeaves = new HashSet<String>();
        for (Block log : found) {
            for (BlockFace face : faces) {
                Block neighbor = log.getRelative(face);
                if (Tag.LEAVES.isTagged(neighbor.getType())) {
                    leafQueue.add(new LeafNode(neighbor, 1));
                }
            }
        }
        while (!leafQueue.isEmpty() && found.size() < limit) {
            LeafNode node = leafQueue.poll();
            Block leaf = node.block();
            if (!visitedLeaves.add(this.blockKey(leaf)) || !Tag.LEAVES.isTagged(leaf.getType())) continue;
            if (node.distance() > 7 || !(leaf.getBlockData() instanceof Leaves leafData)
                || leafData.getDistance() < node.distance()) continue;
            found.add(leaf);
            if (node.distance() == 7) continue;
            for (BlockFace face : faces) {
                Block neighbor = leaf.getRelative(face);
                if (Tag.LEAVES.isTagged(neighbor.getType())) {
                    leafQueue.add(new LeafNode(neighbor, node.distance() + 1));
                }
            }
        }
        return found;
    }

    private record LeafNode(Block block, int distance) {}

    private String blockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}

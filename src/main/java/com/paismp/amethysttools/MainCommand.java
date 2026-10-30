package com.paismp.amethysttools;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class MainCommand
implements CommandExecutor {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private final AmethystToolsPlugin plugin;
    private final NamespacedKey expiresKey;
    private final NamespacedKey pickaxeKey;
    private final NamespacedKey axeKey;
    private final NamespacedKey shovelKey;
    private final NamespacedKey hoeKey;

    public MainCommand(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
        this.expiresKey = plugin.expiresKey();
        this.pickaxeKey = plugin.pickaxeKey();
        this.axeKey = plugin.axeKey();
        this.shovelKey = plugin.shovelKey();
        this.hoeKey = plugin.hoeKey();
    }

    private ItemStack buildTimedTool(Player receiver, Material material, NamespacedKey toolKey, String nameMsgKey, String loreExpireMsgKey, String configDaysPath, Enchantment subEnchantment, int subEnchantmentLevel) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        String name = this.plugin.messages().raw(receiver, nameMsgKey);
        meta.displayName((Component)LEGACY.deserialize(name));
        // meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        meta.setUnbreakable(true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
        meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        if (subEnchantment != null && subEnchantmentLevel > 0) {
            meta.addEnchant(subEnchantment, subEnchantmentLevel, true);
        }

        long days = this.plugin.getConfig().getLong(configDaysPath, 3L);
        long expiresAt = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
        meta.getPersistentDataContainer().set(this.expiresKey, PersistentDataType.LONG, expiresAt);

        String timeFormat = this.plugin.getConfig().getString("time-format", "dd:HH:mm:ss");
        String formattedTime = ExpirationLoreUpdater.formatRemaining(expiresAt, timeFormat);
        String expireLine = this.plugin.messages().raw(receiver, loreExpireMsgKey).replace("{time}", formattedTime);

        meta.lore(List.of(LEGACY.deserialize(expireLine)));
        meta.getPersistentDataContainer().set(toolKey, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);

        return item;
    }

    private void giveAmethystPickaxe(Player target, Enchantment subEnchantment, int subEnchantmentLevel) {
        ItemStack pick = this.buildTimedTool(target,
            Material.NETHERITE_PICKAXE, this.pickaxeKey,
            "item-amethyst-pickaxe-name",
            "item-amethyst-pickaxe-lore-expire",
            "tools.amethyst_pickaxe.days",
            subEnchantment,
            subEnchantmentLevel
        );
        target.getInventory().addItem(new ItemStack[]{pick});
    }

    private void giveAmethystAxe(Player target, Enchantment subEnchantment, int subEnchantmentLevel) {
        ItemStack axe = this.buildTimedTool(target, Material.NETHERITE_AXE, this.axeKey, "item-amethyst-axe-name", "item-amethyst-axe-lore-expire", "tools.amethyst_axe.days", subEnchantment, subEnchantmentLevel);
        target.getInventory().addItem(new ItemStack[]{axe});
    }

    private void giveAmethystShovel(Player target, Enchantment subEnchantment, int subEnchantmentLevel) {
        ItemStack shovel = this.buildTimedTool(target, Material.NETHERITE_SHOVEL, this.shovelKey, "item-amethyst-shovel-name", "item-amethyst-shovel-lore-expire", "tools.amethyst_shovel.days", subEnchantment, subEnchantmentLevel);
        target.getInventory().addItem(new ItemStack[]{shovel});
    }

    private void giveAmethystHoe(Player target) {
        ItemStack hoe = this.buildTimedTool(target, Material.NETHERITE_HOE, this.hoeKey, "item-amethyst-hoe-name", "item-amethyst-hoe-lore-expire", "tools.amethyst_hoe.days", null, 0);
        target.getInventory().addItem(new ItemStack[]{hoe});
    }

    private SubEnchantment parseSubEnchantment(CommandSender sender, String[] args) {
        if (args.length > 3) {
            sender.sendMessage("Use only fortune, silk_touch, or none; enchantment levels are fixed.");
            return null;
        }
        if (args.length < 3 || args[2].equalsIgnoreCase("none")) {
            return new SubEnchantment(null, 0);
        }
        String choice = args[2].toLowerCase(Locale.ROOT);
        Enchantment enchantment;
        int level;
        if (choice.equals("fortune")) {
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune"));
            level = 3;
        } else if (choice.equals("silk_touch")) {
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("silk_touch"));
            level = 1;
        } else {
            sender.sendMessage("Invalid sub-enchantment. Use fortune, silk_touch, or none.");
            return null;
        }
        return enchantment == null ? null : new SubEnchantment(enchantment, level);
    }

    private record SubEnchantment(Enchantment enchantment, int level) {}

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p;
        Player player = p = sender instanceof Player ? (Player)sender : null;
        if (args.length == 0) {
            if (p != null) {
                p.sendMessage(this.plugin.messages().get(p, "usage"));
            } else {
                sender.sendMessage("Usage: /amethysttools <reload|amethyst_pickaxe|amethyst_axe|amethyst_shovel|amethyst_hoe> [player] [fortune|silk_touch|none]; hoes do not support sub-enchantments.");
            }
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (p != null && !p.hasPermission("amethysttools.reload")) {
                p.sendMessage(this.plugin.messages().get(p, "no-permission"));
                return true;
            }
            this.plugin.reloadConfig();
            this.plugin.reloadMessages();
            this.plugin.restartExpirationLoreUpdater();
            if (p != null) {
                p.sendMessage(this.plugin.messages().get(p, "reloaded"));
            } else {
                sender.sendMessage("AmethystTools reloaded.");
            }
            return true;
        }
        if (sub.equals("amethyst_pickaxe")) {
            if (p != null && !p.hasPermission("amethysttools.use")) {
                p.sendMessage(this.plugin.messages().get(p, "no-permission"));
                return true;
            }
            if (args.length >= 2) {
                Player target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                SubEnchantment subEnchantment = this.parseSubEnchantment(sender, args);
                if (subEnchantment == null) {
                    return true;
                }
                this.giveAmethystPickaxe(target, subEnchantment.enchantment(), subEnchantment.level());
                target.sendMessage(this.plugin.messages().get(target, "given"));
                if (p != null) {
                    p.sendMessage("\u00a7aGave Amethyst Pickaxe to \u00a7f" + target.getName());
                } else {
                    sender.sendMessage("Gave Amethyst Pickaxe to " + target.getName());
                }
                return true;
            }
            if (p == null) {
                sender.sendMessage("Console must specify a player: /amethysttools amethyst_pickaxe <player> [fortune|silk_touch|none]");
                return true;
            }
            this.giveAmethystPickaxe(p, null, 0);
            p.sendMessage(this.plugin.messages().get(p, "given"));
            return true;
        }
        if (sub.equals("amethyst_axe")) {
            if (p != null && !p.hasPermission("amethysttools.use")) {
                p.sendMessage(this.plugin.messages().get(p, "no-permission"));
                return true;
            }
            if (args.length >= 2) {
                Player target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                SubEnchantment subEnchantment = this.parseSubEnchantment(sender, args);
                if (subEnchantment == null) {
                    return true;
                }
                this.giveAmethystAxe(target, subEnchantment.enchantment(), subEnchantment.level());
                target.sendMessage(this.plugin.messages().get(target, "given-axe"));
                if (p != null) {
                    p.sendMessage("\u00a7aGave Amethyst Axe to \u00a7f" + target.getName());
                } else {
                    sender.sendMessage("Gave Amethyst Axe to " + target.getName());
                }
                return true;
            }
            if (p == null) {
                sender.sendMessage("Console must specify a player: /amethysttools amethyst_axe <player> [fortune|silk_touch|none]");
                return true;
            }
            this.giveAmethystAxe(p, null, 0);
            p.sendMessage(this.plugin.messages().get(p, "given-axe"));
            return true;
        }
        if (sub.equals("amethyst_shovel")) {
            if (p != null && !p.hasPermission("amethysttools.use")) {
                p.sendMessage(this.plugin.messages().get(p, "no-permission"));
                return true;
            }
            if (args.length >= 2) {
                Player target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                SubEnchantment subEnchantment = this.parseSubEnchantment(sender, args);
                if (subEnchantment == null) {
                    return true;
                }
                this.giveAmethystShovel(target, subEnchantment.enchantment(), subEnchantment.level());
                target.sendMessage(this.plugin.messages().get(target, "given-shovel"));
                if (p != null) {
                    p.sendMessage("\u00a7aGave Amethyst Shovel to \u00a7f" + target.getName());
                } else {
                    sender.sendMessage("Gave Amethyst Shovel to " + target.getName());
                }
                return true;
            }
            if (p == null) {
                sender.sendMessage("Console must specify a player: /amethysttools amethyst_shovel <player> [fortune|silk_touch|none]");
                return true;
            }
            this.giveAmethystShovel(p, null, 0);
            p.sendMessage(this.plugin.messages().get(p, "given-shovel"));
            return true;
        }
        if (sub.equals("amethyst_hoe")) {
            if (p != null && !p.hasPermission("amethysttools.use")) {
                p.sendMessage(this.plugin.messages().get(p, "no-permission"));
                return true;
            }
            if (args.length > 2) {
                sender.sendMessage("The Amethyst Hoe does not support a sub-enchantment.");
                return true;
            }
            if (args.length >= 2) {
                Player target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                this.giveAmethystHoe(target);
                target.sendMessage(this.plugin.messages().get(target, "given-hoe"));
                if (p != null) {
                    p.sendMessage("\u00a7aGave Amethyst Hoe to \u00a7f" + target.getName());
                } else {
                    sender.sendMessage("Gave Amethyst Hoe to " + target.getName());
                }
                return true;
            }
            if (p == null) {
                sender.sendMessage("Console must specify a player: /amethysttools amethyst_hoe <player>");
                return true;
            }
            this.giveAmethystHoe(p);
            p.sendMessage(this.plugin.messages().get(p, "given-hoe"));
            return true;
        }
        if (p != null) {
            p.sendMessage(this.plugin.messages().get(p, "unknown-subcommand"));
            p.sendMessage(this.plugin.messages().get(p, "usage"));
        } else {
            sender.sendMessage("Unknown subcommand.");
            sender.sendMessage("Usage: /amethysttools <reload|amethyst_pickaxe|amethyst_axe|amethyst_shovel|amethyst_hoe> [player]");
        }
        return true;
    }
}

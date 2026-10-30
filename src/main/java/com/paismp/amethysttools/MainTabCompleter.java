package com.paismp.amethysttools;

import com.paismp.amethysttools.AmethystToolsPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class MainTabCompleter
implements TabCompleter {
    private final AmethystToolsPlugin plugin;

    public MainTabCompleter(AmethystToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if ("amethyst_pickaxe".startsWith(input)) {
                completions.add("amethyst_pickaxe");
            }
            if ("amethyst_axe".startsWith(input)) {
                completions.add("amethyst_axe");
            }
            if ("amethyst_shovel".startsWith(input)) {
                completions.add("amethyst_shovel");
            }
            if ("amethyst_hoe".startsWith(input)) {
                completions.add("amethyst_hoe");
            }
            if (sender.hasPermission("amethysttools.reload") && "reload".startsWith(input)) {
                completions.add("reload");
            }
            return completions;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("amethyst_pickaxe") || args[0].equalsIgnoreCase("amethyst_axe") || args[0].equalsIgnoreCase("amethyst_shovel") || args[0].equalsIgnoreCase("amethyst_hoe"))) {
            String input = args[1].toLowerCase();
            for (Player online : Bukkit.getOnlinePlayers()) {
                String name = online.getName();
                if (!name.toLowerCase().startsWith(input)) continue;
                completions.add(name);
            }
            return completions;
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("amethyst_pickaxe") || args[0].equalsIgnoreCase("amethyst_axe") || args[0].equalsIgnoreCase("amethyst_shovel"))) {
            String input = args[2].toLowerCase();
            for (String enchantment : List.of("fortune", "silk_touch", "none")) {
                if (enchantment.startsWith(input)) {
                    completions.add(enchantment);
                }
            }
            return completions;
        }
        return completions;
    }
}

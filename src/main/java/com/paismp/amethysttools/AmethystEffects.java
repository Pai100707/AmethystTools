package com.paismp.amethysttools;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;

final class AmethystEffects {
    private AmethystEffects() {}

    static void playSound(Block block) {
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.75f, 1.0f);
    }

    static void spawnParticles(Block block) {
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        World world = block.getWorld();
        // world.spawnParticle(Particle.BLOCK, location, 12, 0.3, 0.3, 0.3, 0.0,
        //     org.bukkit.Material.AMETHYST_BLOCK.createBlockData());
        world.spawnParticle(Particle.DUST, location, 6, 0.3, 0.3, 0.3, 0.0,
            new Particle.DustOptions(Color.fromRGB(155, 100, 220), 1.2f));
    }
}
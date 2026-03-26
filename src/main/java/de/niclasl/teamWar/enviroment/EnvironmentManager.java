package de.niclasl.teamWar.enviroment;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;

public class EnvironmentManager implements Listener {

    public static void startEnvironmentTask() {
        Bukkit.getScheduler().runTaskTimer(
                Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("TeamWar")),
                EnvironmentManager::applyEnvironmentEffects,
                0L, 100L
        );
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) return;

        World world = shooter.getWorld();
        if (!world.hasStorm()) return;

        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        Vector velocity = arrow.getVelocity().clone();
        double spread = world.isThundering() ? 0.2 : 0.1;
        velocity.add(new Vector(
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread
        ));
        arrow.setVelocity(velocity);

        double modifier = world.isThundering() ? 0.9 : 0.8;
        arrow.setDamage(arrow.getDamage() * modifier);

        arrow.getWorld().spawnParticle(
                Particle.DRIPPING_WATER,
                arrow.getLocation(),
                5, 0.1, 0.1, 0.1, 0.01
        );
    }

    private static void applyEnvironmentEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();

            clearEnvironmentEffects(player);

            if (world.isThundering()) {
                applyThunderEffects(player);
            } else if (world.hasStorm()) {
                applyRainEffects(player);
            } else {
                applyClearWeatherEffects(player);
            }

            applyBiomeEffects(player);
        }
    }

    private static void applyBiomeEffects(Player player) {
        org.bukkit.block.Biome biome = player.getLocation().getBlock().getBiome();

        if (biome == org.bukkit.block.Biome.SNOWY_PLAINS ||
                biome == org.bukkit.block.Biome.ICE_SPIKES ||
                biome == org.bukkit.block.Biome.FROZEN_PEAKS ||
                biome == org.bukkit.block.Biome.GROVE) {
            applySnowEffects(player);
        }
        else if (biome == org.bukkit.block.Biome.DESERT ||
                biome == org.bukkit.block.Biome.BADLANDS ||
                biome == org.bukkit.block.Biome.SAVANNA) {
            applyDesertEffects(player);
        }
        else if (biome == org.bukkit.block.Biome.JUNGLE ||
                biome == org.bukkit.block.Biome.BAMBOO_JUNGLE ||
                biome == org.bukkit.block.Biome.SPARSE_JUNGLE) {
            applyJungleEffects(player);
        }
    }

    private static void clearEnvironmentEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.STRENGTH);
    }

    private static void applyClearWeatherEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0, true, false));
    }

    private static void applyRainEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0, true, false));
    }

    private static void applyThunderEffects(Player player) {
        // Sturm -> Aggressiver Nahkampf
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0, true, false));
    }

    private static void applySnowEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0, true, false));
    }

    private static void applyDesertEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 0, true, false));
    }

    private static void applyJungleEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 120, 0, true, false));
        if (Math.random() < 0.05) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, true, false));
        }
    }
}

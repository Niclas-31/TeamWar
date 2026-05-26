package de.niclasl.teamWar.strength.manager;

import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;

public class StrengthManager {

    public static int getStrength(Player player) {
        int strength = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                strength += getItemStrength(item);
            }
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                strength += getItemStrength(armor);
            }
        }

        return strength;
    }

    public static int getTeamStrength(Team team) {
        int total = 0;
        for (UUID uuid : team.getMembers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                total += getStrength(player);
            }
        }
        return total;
    }

    private static int getItemStrength(ItemStack item) {
        Material mat = item.getType();
        int points = 0;

        switch (mat) {
            case WOODEN_SWORD, IRON_HELMET, FIREWORK_ROCKET: points += 5; break;
            case STONE_SWORD, NETHERITE_BOOTS, DIAMOND_HELMET, IRON_CHESTPLATE, SHIELD: points += 8; break;
            case IRON_SWORD, NETHERITE_LEGGINGS, DIAMOND_CHESTPLATE, CROSSBOW: points += 12; break;
            case DIAMOND_SWORD: points += 18; break;
            case NETHERITE_SWORD: points += 22; break;

            case WOODEN_AXE, DIAMOND_BOOTS, IRON_LEGGINGS: points += 6; break;
            case STONE_AXE: points += 9; break;
            case IRON_AXE: points += 13; break;
            case DIAMOND_AXE: points += 17; break;
            case NETHERITE_AXE, END_CRYSTAL: points += 20; break;

            case BOW, TNT, NETHERITE_HELMET, DIAMOND_LEGGINGS: points += 10; break;
            case TRIDENT: points += 16; break;
            case MACE: points += 25; break;

            case TOTEM_OF_UNDYING, NETHERITE_CHESTPLATE: points += 15; break;

            case LEATHER_HELMET, LEATHER_LEGGINGS: points += 2; break;
            case LEATHER_CHESTPLATE, IRON_BOOTS: points += 3; break;
            case LEATHER_BOOTS: points += 1; break;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Map.Entry<Enchantment, Integer> ench : meta.getEnchants().entrySet()) {
                points += getEnchantmentBonus(ench.getKey(), ench.getValue());
            }
        }

        return points;
    }

    private static int getEnchantmentBonus(Enchantment ench, int level) {
        return switch (ench.getKeyOrThrow().getKey()) {
            case "sharpness", "smite", "bane_of_arthropods", "power", "protection", "fire_protection",
                 "projectile_protection", "blast_protection" -> 2 * level;
            case "infinity", "flame" -> 3 ;
            case "mending" -> 2;
            default -> level;
        };
    }
}

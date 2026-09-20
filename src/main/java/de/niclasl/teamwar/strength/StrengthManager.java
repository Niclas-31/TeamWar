package de.niclasl.teamwar.strength;

import de.niclasl.teamwar.team.Team;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.UUID;

public class StrengthManager {

    private final MinecraftServer server;

    public StrengthManager(MinecraftServer server) {
        this.server = server;
    }

    public int getStrength(Player player) {
        int strength = 0;

        var inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack item = inventory.getItem(slot);

            if (!item.isEmpty()) {
                strength += getItemStrength(item);
            }
        }

        return strength;
    }

    public int getTeamStrength(Team team) {
        int total = 0;

        for (UUID uuid : team.getMembers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player != null) {
                total += getStrength(player);
            }
        }
        return total;
    }

    private static int getItemStrength(ItemStack item) {
        int points = 0;

        Item itemType = item.getItem();

        if (itemType == Items.WOODEN_SWORD
                || itemType == Items.IRON_HELMET
                || itemType == Items.FIREWORK_ROCKET) {
            points += 5;
        } else if (itemType == Items.STONE_SWORD
                || itemType == Items.NETHERITE_BOOTS
                || itemType == Items.DIAMOND_HELMET
                || itemType == Items.IRON_CHESTPLATE
                || itemType == Items.SHIELD) {
            points += 8;
        } else if (itemType == Items.IRON_SWORD
                || itemType == Items.NETHERITE_LEGGINGS
                || itemType == Items.DIAMOND_CHESTPLATE
                || itemType == Items.CROSSBOW) {
            points += 12;
        } else if (itemType == Items.DIAMOND_SWORD) {
            points += 18;
        } else if (itemType == Items.NETHERITE_SWORD) {
            points += 22;
        } else if (itemType == Items.WOODEN_AXE
                || itemType == Items.DIAMOND_BOOTS
                || itemType == Items.IRON_LEGGINGS) {
            points += 6;
        } else if (itemType == Items.STONE_AXE) {
            points += 9;
        } else if (itemType == Items.IRON_AXE) {
            points += 13;
        } else if (itemType == Items.DIAMOND_AXE) {
            points += 17;
        } else if (itemType == Items.NETHERITE_AXE
                || itemType == Items.END_CRYSTAL) {
            points += 20;
        } else if (itemType == Items.BOW
                || itemType == Items.TNT
                || itemType == Items.NETHERITE_HELMET
                || itemType == Items.DIAMOND_LEGGINGS) {
            points += 10;
        } else if (itemType == Items.TRIDENT) {
            points += 16;
        } else if (itemType == Items.MACE) {
            points += 25;
        } else if (itemType == Items.TOTEM_OF_UNDYING
                || itemType == Items.NETHERITE_CHESTPLATE) {
            points += 15;
        } else if (itemType == Items.LEATHER_HELMET
                || itemType == Items.LEATHER_LEGGINGS) {
            points += 2;
        } else if (itemType == Items.LEATHER_CHESTPLATE
                ||  itemType == Items.IRON_BOOTS) {
            points += 3;
        } else if (itemType == Items.LEATHER_BOOTS) {
            points += 1;
        }

        points += getEnchantmentBonus(item);

        return points;
    }

    private static int getEnchantmentBonus(ItemStack item) {
        ItemEnchantments enchantments = item.getTagEnchantments();

        if (enchantments.isEmpty()) {
            return 0;
        }

        int bonus = 0;

        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int level = enchantments.getLevel(enchantment);

            bonus += getEnchantmentBonus(enchantment, level);
        }

        return bonus;
    }

    private static int getEnchantmentBonus(Holder<Enchantment> enchantment, int level) {
        String id = enchantment.getRegisteredName();

        return switch (id) {
            case "minecraft:sharpness",
                 "minecraft:smite",
                 "minecraft:bane_of_arthropods",
                 "minecraft:power",
                 "minecraft:protection",
                 "minecraft:fire_protection",
                 "minecraft:projectile_protection",
                 "minecraft:blast_protection" -> 2 * level;

            case "minecraft:infinity",
                 "minecraft:flame" -> 3;

            case "minecraft:mending" -> 2;

            default -> level;
        };
    }
}
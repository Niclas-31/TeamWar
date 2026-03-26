package de.niclasl.teamWar.money;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockValue {

    private static final Map<Material, ValueRange> values = new HashMap<>();

    static {
        values.put(Material.DIAMOND_ORE, new ValueRange(90, 110));
        values.put(Material.DEEPSLATE_DIAMOND_ORE, new ValueRange(90, 110));
        values.put(Material.EMERALD_ORE, new ValueRange(70, 90));
        values.put(Material.DEEPSLATE_EMERALD_ORE, new ValueRange(70, 90));
        values.put(Material.GOLD_ORE, new ValueRange(40, 60));
        values.put(Material.DEEPSLATE_GOLD_ORE, new ValueRange(40, 60));
        values.put(Material.IRON_ORE, new ValueRange(15, 25));
        values.put(Material.DEEPSLATE_IRON_ORE, new ValueRange(15, 25));
        values.put(Material.COAL_ORE, new ValueRange(5, 15));
        values.put(Material.DEEPSLATE_COAL_ORE, new ValueRange(5, 15));
        values.put(Material.LAPIS_ORE, new ValueRange(10, 20));
        values.put(Material.DEEPSLATE_LAPIS_ORE, new ValueRange(10, 20));
        values.put(Material.REDSTONE_ORE, new ValueRange(20, 30));
        values.put(Material.DEEPSLATE_REDSTONE_ORE, new ValueRange(20, 30));
        values.put(Material.COPPER_ORE, new ValueRange(5, 15));
        values.put(Material.DEEPSLATE_COPPER_ORE, new ValueRange(5, 15));

        values.put(Material.STONE, new ValueRange(1, 3));
        values.put(Material.DEEPSLATE, new ValueRange(1, 3));
        values.put(Material.COBBLESTONE, new ValueRange(1, 2));
        values.put(Material.GRAVEL, new ValueRange(1, 2));
        values.put(Material.SAND, new ValueRange(1, 2));
        values.put(Material.DIRT, new ValueRange(1, 2));
        values.put(Material.COARSE_DIRT, new ValueRange(1, 2));
        values.put(Material.PODZOL, new ValueRange(1, 2));
        values.put(Material.MYCELIUM, new ValueRange(1, 2));
        values.put(Material.CLAY, new ValueRange(2, 4));
        values.put(Material.SNOW_BLOCK, new ValueRange(1, 2));

        values.put(Material.OAK_LOG, new ValueRange(1, 3));
        values.put(Material.SPRUCE_LOG, new ValueRange(1, 3));
        values.put(Material.BIRCH_LOG, new ValueRange(1, 3));
        values.put(Material.JUNGLE_LOG, new ValueRange(1, 3));
        values.put(Material.ACACIA_LOG, new ValueRange(1, 3));
        values.put(Material.DARK_OAK_LOG, new ValueRange(1, 3));
        values.put(Material.STRIPPED_OAK_LOG, new ValueRange(1, 2));
        values.put(Material.STRIPPED_SPRUCE_LOG, new ValueRange(1, 2));
        values.put(Material.STRIPPED_BIRCH_LOG, new ValueRange(1, 2));
        values.put(Material.STRIPPED_JUNGLE_LOG, new ValueRange(1, 2));
        values.put(Material.STRIPPED_ACACIA_LOG, new ValueRange(1, 2));
        values.put(Material.STRIPPED_DARK_OAK_LOG, new ValueRange(1, 2));

        values.put(Material.OAK_LEAVES, new ValueRange(1, 2));
        values.put(Material.SPRUCE_LEAVES, new ValueRange(1, 2));
        values.put(Material.BIRCH_LEAVES, new ValueRange(1, 2));
        values.put(Material.JUNGLE_LEAVES, new ValueRange(1, 2));
        values.put(Material.ACACIA_LEAVES, new ValueRange(1, 2));
        values.put(Material.DARK_OAK_LEAVES, new ValueRange(1, 2));

        values.put(Material.GRASS_BLOCK, new ValueRange(1, 2));
        values.put(Material.FERN, new ValueRange(1, 2));
        values.put(Material.TALL_GRASS, new ValueRange(1, 2));
        values.put(Material.SUGAR_CANE, new ValueRange(2, 4));
        values.put(Material.CACTUS, new ValueRange(2, 4));
        values.put(Material.MELON, new ValueRange(2, 5));
        values.put(Material.PUMPKIN, new ValueRange(2, 5));

        values.put(Material.COBWEB, new ValueRange(4, 6));
        values.put(Material.BAMBOO, new ValueRange(1, 2));
        values.put(Material.NETHERRACK, new ValueRange(1, 2));
        values.put(Material.SOUL_SAND, new ValueRange(1, 3));
        values.put(Material.SOUL_SOIL, new ValueRange(1, 3));
        values.put(Material.SANDSTONE, new ValueRange(2, 3));
        values.put(Material.RED_SANDSTONE, new ValueRange(2, 3));
        values.put(Material.ICE, new ValueRange(1, 2));
        values.put(Material.PACKED_ICE, new ValueRange(2, 3));
        values.put(Material.SNOW, new ValueRange(1, 2));

        values.put(Material.NETHER_QUARTZ_ORE, new ValueRange(25, 35));
        values.put(Material.ANCIENT_DEBRIS, new ValueRange(180, 220));
        values.put(Material.END_STONE, new ValueRange(1, 2));
    }

    public static int getValue(Material blockType) {
        ValueRange range = values.get(blockType);
        if (range == null) return 0;
        return range.getRandom();
    }
}
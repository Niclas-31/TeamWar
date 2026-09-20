package de.niclasl.teamwar.economy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class BlockValue {

    private static final Map<Block, ValueRange> values = new HashMap<>();

    static {
        values.put(Blocks.DIAMOND_ORE, new ValueRange(90, 110));
        values.put(Blocks.DEEPSLATE_DIAMOND_ORE, new ValueRange(90, 110));
        values.put(Blocks.EMERALD_ORE, new ValueRange(70, 90));
        values.put(Blocks.DEEPSLATE_EMERALD_ORE, new ValueRange(70, 90));
        values.put(Blocks.GOLD_ORE, new ValueRange(40, 60));
        values.put(Blocks.DEEPSLATE_GOLD_ORE, new ValueRange(40, 60));
        values.put(Blocks.IRON_ORE, new ValueRange(15, 25));
        values.put(Blocks.DEEPSLATE_IRON_ORE, new ValueRange(15, 25));
        values.put(Blocks.COAL_ORE, new ValueRange(5, 15));
        values.put(Blocks.DEEPSLATE_COAL_ORE, new ValueRange(5, 15));
        values.put(Blocks.LAPIS_ORE, new ValueRange(10, 20));
        values.put(Blocks.DEEPSLATE_LAPIS_ORE, new ValueRange(10, 20));
        values.put(Blocks.REDSTONE_ORE, new ValueRange(20, 30));
        values.put(Blocks.DEEPSLATE_REDSTONE_ORE, new ValueRange(20, 30));
        values.put(Blocks.COPPER_ORE, new ValueRange(5, 15));
        values.put(Blocks.DEEPSLATE_COPPER_ORE, new ValueRange(5, 15));

        values.put(Blocks.STONE, new ValueRange(1, 3));
        values.put(Blocks.DEEPSLATE, new ValueRange(1, 3));
        values.put(Blocks.COBBLESTONE, new ValueRange(1, 2));
        values.put(Blocks.GRAVEL, new ValueRange(1, 2));
        values.put(Blocks.SAND, new ValueRange(1, 2));
        values.put(Blocks.DIRT, new ValueRange(1, 2));
        values.put(Blocks.COARSE_DIRT, new ValueRange(1, 2));
        values.put(Blocks.PODZOL, new ValueRange(1, 2));
        values.put(Blocks.MYCELIUM, new ValueRange(1, 2));
        values.put(Blocks.CLAY, new ValueRange(2, 4));
        values.put(Blocks.SNOW_BLOCK, new ValueRange(1, 2));

        values.put(Blocks.OAK_LOG, new ValueRange(1, 3));
        values.put(Blocks.SPRUCE_LOG, new ValueRange(1, 3));
        values.put(Blocks.BIRCH_LOG, new ValueRange(1, 3));
        values.put(Blocks.JUNGLE_LOG, new ValueRange(1, 3));
        values.put(Blocks.ACACIA_LOG, new ValueRange(1, 3));
        values.put(Blocks.DARK_OAK_LOG, new ValueRange(1, 3));
        values.put(Blocks.STRIPPED_OAK_LOG, new ValueRange(1, 2));
        values.put(Blocks.STRIPPED_SPRUCE_LOG, new ValueRange(1, 2));
        values.put(Blocks.STRIPPED_BIRCH_LOG, new ValueRange(1, 2));
        values.put(Blocks.STRIPPED_JUNGLE_LOG, new ValueRange(1, 2));
        values.put(Blocks.STRIPPED_ACACIA_LOG, new ValueRange(1, 2));
        values.put(Blocks.STRIPPED_DARK_OAK_LOG, new ValueRange(1, 2));

        values.put(Blocks.OAK_LEAVES, new ValueRange(1, 2));
        values.put(Blocks.SPRUCE_LEAVES, new ValueRange(1, 2));
        values.put(Blocks.BIRCH_LEAVES, new ValueRange(1, 2));
        values.put(Blocks.JUNGLE_LEAVES, new ValueRange(1, 2));
        values.put(Blocks.ACACIA_LEAVES, new ValueRange(1, 2));
        values.put(Blocks.DARK_OAK_LEAVES, new ValueRange(1, 2));

        values.put(Blocks.GRASS_BLOCK, new ValueRange(1, 2));
        values.put(Blocks.FERN, new ValueRange(1, 2));
        values.put(Blocks.TALL_GRASS, new ValueRange(1, 2));
        values.put(Blocks.SUGAR_CANE, new ValueRange(2, 4));
        values.put(Blocks.CACTUS, new ValueRange(2, 4));
        values.put(Blocks.MELON, new ValueRange(2, 5));
        values.put(Blocks.PUMPKIN, new ValueRange(2, 5));

        values.put(Blocks.COBWEB, new ValueRange(4, 6));
        values.put(Blocks.BAMBOO, new ValueRange(1, 2));
        values.put(Blocks.NETHERRACK, new ValueRange(1, 2));
        values.put(Blocks.SOUL_SAND, new ValueRange(1, 3));
        values.put(Blocks.SOUL_SOIL, new ValueRange(1, 3));
        values.put(Blocks.SANDSTONE, new ValueRange(2, 3));
        values.put(Blocks.RED_SANDSTONE, new ValueRange(2, 3));
        values.put(Blocks.ICE, new ValueRange(1, 2));
        values.put(Blocks.PACKED_ICE, new ValueRange(2, 3));
        values.put(Blocks.SNOW, new ValueRange(1, 2));

        values.put(Blocks.NETHER_QUARTZ_ORE, new ValueRange(25, 35));
        values.put(Blocks.ANCIENT_DEBRIS, new ValueRange(180, 220));
        values.put(Blocks.END_STONE, new ValueRange(1, 2));
    }

    public static int getValue(Block blockType) {
        ValueRange range = values.get(blockType);
        if (range == null) return 0;
        return range.getRandom();
    }
}
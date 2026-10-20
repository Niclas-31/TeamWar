package de.niclasl.teamwar.registries.blocks;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.registries.blocks.custom.CorruptCommandBlock;
import de.niclasl.teamwar.registries.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TeamWar.MOD_ID);

    public static final DeferredBlock<Block> CORRUPTED_COMMAND_BLOCK = registerBlock(
            "corrupted_command_block",
            (properties) -> new CorruptCommandBlock(properties.mapColor(MapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(100.0F, 3600000.0F).noLootTable())
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, (properties) -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
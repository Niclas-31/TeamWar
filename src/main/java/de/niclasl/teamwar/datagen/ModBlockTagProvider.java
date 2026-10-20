package de.niclasl.teamwar.datagen;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.util.ModBlockItemIds;
import de.niclasl.teamwar.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

  public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, lookupProvider, TeamWar.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.@NonNull Provider provider) {
    tag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL)
            .add(ModBlockItemIds.CORRUPTED_COMMAND_BLOCK.block());
    tag(ModTags.Blocks.INCORRECT_FOR_CORRUPTED_TOOL);

    tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlockItemIds.CORRUPTED_COMMAND_BLOCK.block());

    tag(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_STONE_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_COPPER_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_GOLD_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_IRON_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
    tag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)
            .addTag(ModTags.Blocks.NEEDS_CORRUPTED_TOOL);
  }
}
package de.niclasl.teamwar.util;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

  public static class Blocks {
    public static final TagKey<Block> NEEDS_CORRUPTED_TOOL = createTag("needs_corrupted_tool");
    public static final TagKey<Block> INCORRECT_FOR_CORRUPTED_TOOL = createTag("incorrect_for_corrupted_tool");

    private static TagKey<Block> createTag(String name) {
      return BlockTags.create(Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, name));
    }
  }
}
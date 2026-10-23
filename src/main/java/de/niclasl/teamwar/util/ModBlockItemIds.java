package de.niclasl.teamwar.util;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public class ModBlockItemIds {
  public static final BlockItemId CORRUPTED_COMMAND_BLOCK = create("corrupted_command_block");

  public static BlockItemId create(String name) {
    Identifier id = Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, name);
    return BlockItemId.create(id, id);
  }
}
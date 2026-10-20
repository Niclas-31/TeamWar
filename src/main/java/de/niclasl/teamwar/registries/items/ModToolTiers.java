package de.niclasl.teamwar.registries.items;

import de.niclasl.teamwar.util.ModTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class ModToolTiers {
    public static final ToolMaterial CORRUPTED = new ToolMaterial(
            ModTags.Blocks.INCORRECT_FOR_CORRUPTED_TOOL,
            4096,
            16.0F,
            8.0F,
            25,
            ItemTags.NETHERITE_TOOL_MATERIALS
    );
}
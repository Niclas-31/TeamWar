package de.niclasl.teamwar.registries.items;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TeamWar.MOD_ID);

    public static final DeferredItem<Item> CORRUPTED_PICKAXE = ITEMS.registerItem(
            "corrupted_pickaxe",
            (properties) -> new Item(properties.pickaxe(ModToolTiers.CORRUPTED, 8.0F, 1.2F))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
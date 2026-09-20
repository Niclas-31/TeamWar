package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class Blocker {

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        Block type = event.getPlacedBlock().getBlock();

        if (type == Blocks.RESPAWN_ANCHOR) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("Respawn Anchors are disabled!")
                        .withStyle(ChatFormatting.RED)
                );
            }
        } else if (type == Blocks.TNT) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("TNT are disabled!")
                        .withStyle(ChatFormatting.RED)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onCrystalPlace(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().equals(new ItemStack(Items.END_CRYSTAL))) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("End Crystals are disabled!")
                        .withStyle(ChatFormatting.RED)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onMinecartPlace(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().equals(new ItemStack(Items.TNT_MINECART))) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("TNT Minecarts are disabled!")
                        .withStyle(ChatFormatting.RED)
                );
            }
        }
    }
}
package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ForbiddenItems {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            checkInventory(player);
        }
    }

    private static void checkInventory(ServerPlayer player) {
        if (player.isCreative()) {
            return;
        }

        if (player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
            return;
        }

        for (ItemStack item : player.getInventory().getNonEquipmentItems()) {
            if (item.isEmpty()) {
                continue;
            }

            ItemEnchantments enchantments = item.getTagEnchantments();

            if (enchantments.isEmpty()) {
                continue;
            }

            List<Holder<Enchantment>> invalidEnchantments = new ArrayList<>();

            enchantments.keySet().forEach(enchantment -> {
                int level = enchantments.getLevel(enchantment);

                if (level > enchantment.value().getMaxLevel()) {
                    invalidEnchantments.add(enchantment);
                }
            });

            for (Holder<Enchantment> enchantment : invalidEnchantments) {
                int maxLevel = enchantment.value().getMaxLevel();

                ItemEnchantments.Mutable mutable =
                        new ItemEnchantments.Mutable(item.getTagEnchantments());

                mutable.set(enchantment, maxLevel);

                item.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

                player.sendSystemMessage(
                        Component.literal(
                                "Invalid enchantment removed: "
                                        + enchantment.getRegisteredName()
                        ).withStyle(ChatFormatting.RED)
                );
            }
        }
    }
}
package de.niclasl.teamwar.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.menu.UpgradeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class UpgradeCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("upgrade")
                        .executes(context -> {
                            upgrade(context.getSource());
                            return 1;
                        })
        );
    }

    private static void upgrade(CommandSourceStack source) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        UpgradeMenu.open(player);
    }
}
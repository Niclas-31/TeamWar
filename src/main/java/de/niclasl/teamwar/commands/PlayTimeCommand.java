package de.niclasl.teamwar.commands;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.player.PlayerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class PlayTimeCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("playtime")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            PlayerManager playerManager = TeamWar.getPlayerManager();

                            long seconds = playerManager.getDailyPlayTime(player.getUUID());

                            player.sendSystemMessage(
                                    Component.translatable("message.command.playtime.your_playtime",
                                            formatTime(seconds))
                            );
                            return 1;
                        })
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();

                                    Collection<ServerPlayer> players =
                                            EntityArgument.getPlayers(context, "players");

                                    for (ServerPlayer player : players) {

                                        if (player == null) {
                                            sender.sendSystemMessage(
                                                    Component.translatable("message.command.playtime.player_not_found")
                                                    .withStyle(ChatFormatting.RED)
                                            );
                                            return 0;
                                        }

                                        PlayerManager playerManager = TeamWar.getPlayerManager();

                                        long seconds = playerManager.getDailyPlayTime(player.getUUID());

                                        sender.sendSystemMessage(Component.translatable(
                                                "message.command.playtime.other_playtime",
                                                player.getName().getString(),
                                                formatTime(seconds))
                                        );
                                    }

                                    return 1;
                                })
                        )
        );
    }

    private static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, sec);
    }
}
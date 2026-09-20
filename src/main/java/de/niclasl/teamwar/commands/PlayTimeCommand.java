package de.niclasl.teamwar.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.player.PlayerManager;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
                                    Component.literal(
                                            "Your playing time today: "
                                                    + formatTime(seconds))
                            );
                            return 1;
                        })
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> {
                                    ServerPlayer sender =
                                            context.getSource()
                                                    .getPlayerOrException();

                                    String playerName =
                                            StringArgumentType.getString(context, "players");

                                    ServerPlayer target =
                                            sender.level().getServer()
                                                    .getPlayerList()
                                                    .getPlayerByName(
                                                            playerName
                                                    );

                                    if (target == null) {
                                        sender.sendSystemMessage(
                                                Component.literal(
                                                        "Player not found!"
                                                )
                                        );
                                        return 0;
                                    }

                                    PlayerManager playerManager =
                                            TeamWar.getPlayerManager();

                                    long seconds =
                                            playerManager.getDailyPlayTime(
                                                    target.getUUID()
                                            );

                                    sender.sendSystemMessage(
                                            Component.literal(
                                                    target.getName()
                                                            .getString()
                                                            + " today's game time: "
                                                            + formatTime(
                                                            seconds
                                                    )
                                            )
                                    );

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
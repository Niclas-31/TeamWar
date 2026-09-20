package de.niclasl.teamwar.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.PlayerClass;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class ClassCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("class")
                        .then(Commands.argument("class", StringArgumentType.word())
                                .suggests((_, builder) -> {
                                    for (PlayerClass playerClass : PlayerClass.values()) {
                                        builder.suggest(playerClass.name().toLowerCase(Locale.ROOT));
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String value = StringArgumentType.getString(context, "class");
                                    PlayerClass playerClass;

                                    try {
                                        playerClass = PlayerClass.valueOf(
                                                value.toUpperCase(Locale.ROOT)
                                        );
                                    } catch (IllegalArgumentException e) {
                                        player.sendSystemMessage(
                                                Component.literal("Unknown class: " + value)
                                                        .withStyle(ChatFormatting.RED)
                                        );

                                        return 0;
                                    }

                                    classType(context.getSource(), playerClass);
                                    return 1;
                                }))
        );
    }

    private static void classType(CommandSourceStack source, PlayerClass playerClass) {
        TeamManager teamManager = TeamWar.getTeamManager();
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

        Team team = teamManager.getTeam(player.getUUID());
        if (team == null) {
            player.sendSystemMessage(Component.literal("You are not in a team!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (team.hasClass(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You already have a class and cannot switch!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        String className = playerClass.name().toLowerCase();
        List<String> validClasses = Arrays.asList("healer", "tank", "miner", "archer", "builder");
        if (!validClasses.contains(className)) {
            player.sendSystemMessage(Component.literal(
                    "Invalid class! Valid classes: healer, tank, miner, archer, builder"
                    ).withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!team.assignClass(className, player.getUUID())) {
            player.sendSystemMessage(Component.literal("This class is already taken in your team!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        teamManager.setPlayerClass(player, playerClass);

        player.sendSystemMessage(Component.literal("You are now a " + className + "!")
                .withStyle(ChatFormatting.GREEN)
        );
    }
}
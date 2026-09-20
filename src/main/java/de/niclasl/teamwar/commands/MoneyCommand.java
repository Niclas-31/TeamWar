package de.niclasl.teamwar.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.economy.MoneyManager;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class MoneyCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("eco")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.literal("give")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(context -> {
                                                    ServerPlayer target =
                                                            EntityArgument.getPlayer(context, "player");
                                                    double amount =
                                                            DoubleArgumentType.getDouble(context, "amount");
                                                    give(context.getSource(), target, amount);
                                                    return 1;
                                                }))
                                )
                        )
                        .then(Commands.literal("take")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(context -> {
                                                    ServerPlayer target =
                                                            EntityArgument.getPlayer(context, "player");
                                                    double amount =
                                                            DoubleArgumentType.getDouble(context, "amount");
                                                    take(context.getSource(), target, amount);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                .executes(context -> {
                                                    ServerPlayer target =
                                                            EntityArgument.getPlayer(context, "player");
                                                    double amount =
                                                            DoubleArgumentType.getDouble(context, "amount");
                                                    set(context.getSource(), target, amount);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static void give(CommandSourceStack source, ServerPlayer target, double amount) {
        ServerPlayer sender;

        try {
            sender = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (sender == target) {
            sender.sendSystemMessage(
                    Component.literal("You cannot give money to yourself!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        MoneyManager money = TeamWar.getMoneyManager();
        TeamManager teams = TeamWar.getTeamManager();

        String senderTeam = teams.getTeamOfPlayer(sender.getUUID());
        String targetTeam = teams.getTeamOfPlayer(target.getUUID());

        if (senderTeam != null) {

            if (money.getTeamMoney(senderTeam) < amount) {
                sender.sendSystemMessage(
                        Component.literal("Not enough money in your team!")
                                .withStyle(ChatFormatting.RED)
                );
                return;
            }

            money.removeTeamMoney(senderTeam, amount);

        } else {

            if (money.getPlayerMoney(sender.getUUID()) < amount) {
                sender.sendSystemMessage(
                        Component.literal("You don't have enough money!")
                                .withStyle(ChatFormatting.RED)
                );
                return;
            }

            money.removePlayerMoney(sender.getUUID(), amount);
        }

        if (targetTeam != null) {
            money.addTeamMoney(targetTeam, amount);
        } else {
            money.addPlayerMoney(target.getUUID(), amount);
        }

        String formatted = format(amount);

        sender.sendSystemMessage(
                Component.literal(
                        "Gave " + formatted + "$ to " + target.getGameProfile().name()
                ).withStyle(ChatFormatting.GREEN)
        );

        target.sendSystemMessage(
                Component.literal(
                        "You received " + formatted + "$ from "
                                + sender.getGameProfile().name()
                ).withStyle(ChatFormatting.GREEN)
        );
    }

    private static void take(CommandSourceStack source, ServerPlayer target, double amount) {
        ServerPlayer sender;

        try {
            sender = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        MoneyManager money = TeamWar.getMoneyManager();
        TeamManager teams = TeamWar.getTeamManager();

        String targetTeam = teams.getTeamOfPlayer(target.getUUID());

        if (targetTeam != null) {
            money.removeTeamMoney(targetTeam, amount);
        } else {
            money.removePlayerMoney(target.getUUID(), amount);
        }

        String formatted = format(amount);

        sender.sendSystemMessage(
                Component.literal(
                        "Removed " + formatted + "$ from "
                                + target.getGameProfile().name()
                ).withStyle(ChatFormatting.GREEN)
        );

        target.sendSystemMessage(
                Component.literal(
                        formatted + "$ were removed by "
                                + sender.getGameProfile().name()
                ).withStyle(ChatFormatting.RED)
        );
    }

    private static void set(CommandSourceStack source, ServerPlayer target, double amount) {
        ServerPlayer sender;

        try {
            sender = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        MoneyManager money = TeamWar.getMoneyManager();
        TeamManager teams = TeamWar.getTeamManager();

        String targetTeam = teams.getTeamOfPlayer(target.getUUID());

        if (targetTeam != null) {

            money.setTeamMoney(targetTeam, amount);

            sender.sendSystemMessage(
                    Component.literal(
                            "Set " + target.getGameProfile().name()
                                    + "'s team money to " + format(amount) + "$"
                    ).withStyle(ChatFormatting.GREEN)
            );

            target.sendSystemMessage(
                    Component.literal(
                            "Your team's balance was set to "
                                    + format(amount)
                                    + "$ by "
                                    + sender.getGameProfile().name()
                    ).withStyle(ChatFormatting.GREEN)
            );

        } else {

            money.setPlayerMoney(target.getUUID(), amount);

            sender.sendSystemMessage(
                    Component.literal(
                            "Set " + target.getGameProfile().name()
                                    + "'s money to " + format(amount) + "$"
                    ).withStyle(ChatFormatting.GREEN)
            );

            target.sendSystemMessage(
                    Component.literal(
                            "Your balance was set to "
                                    + format(amount)
                                    + "$ by "
                                    + sender.getGameProfile().name()
                    ).withStyle(ChatFormatting.GREEN)
            );
        }
    }

    private static String format(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}
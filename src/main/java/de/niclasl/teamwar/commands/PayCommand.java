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

import java.math.BigDecimal;
import java.math.RoundingMode;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class PayCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> {
                                            ServerPlayer target =
                                                    EntityArgument.getPlayer(context, "player");
                                            double amount =
                                                    DoubleArgumentType.getDouble(context, "amount");
                                            pay(context.getSource(), target, amount);
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static void pay(CommandSourceStack source, ServerPlayer target, double rawAmount) {
        TeamManager teamManager = TeamWar.getTeamManager();
        MoneyManager moneyManager = TeamWar.getMoneyManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.translatable("message.command.only_players")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (player.equals(target)) {
            player.sendSystemMessage(
                    Component.translatable("message.command.pay.cannot_pay_yourself")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(rawAmount).setScale(2, RoundingMode.HALF_UP);

        String playerTeam = teamManager.getTeamOfPlayer(player.getUUID());

        String targetTeam = teamManager.getTeamOfPlayer(target.getUUID());

        if (playerTeam == null) {
            player.sendSystemMessage(
                    Component.translatable("message.not_in_team")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (targetTeam == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.command.pay.target.not_in_team", target.getGameProfile().name()
            ).withStyle(ChatFormatting.RED));
            return;
        }

        double payment = amount.doubleValue();

        if (moneyManager.getTeamMoney(playerTeam) < payment) {
            player.sendSystemMessage(
                    Component.translatable("message.command.pay.not_enough_money")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        moneyManager.removeTeamMoney(playerTeam, payment);
        moneyManager.addTeamMoney(targetTeam, payment);

        String formattedAmount = MoneyManager.formatMoney(payment);

        player.sendSystemMessage(
                Component.translatable(
                        "message.command.pay.sender.paid",
                        formattedAmount, target.getGameProfile().name()
                ).withStyle(ChatFormatting.GREEN)
        );

        target.sendSystemMessage(
                Component.translatable(
                        "message.command.pay.target.received",
                        formattedAmount, player.getGameProfile().name()
                ).withStyle(ChatFormatting.GREEN)
        );
    }
}
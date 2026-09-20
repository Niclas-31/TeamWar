package de.niclasl.teamwar.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.classes.ClassUpgrade;
import de.niclasl.teamwar.classes.PlayerClass;
import de.niclasl.teamwar.classes.PlayerUpgrades;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class HealCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("heal")
                        .executes(context -> {
                            heal(context.getSource());
                            return 1;
                        })
        );
    }

    private static void heal(CommandSourceStack source) {
        TeamManager teamManager = TeamWar.getTeamManager();
        ClassManager classManager = TeamWar.getClassManager();
        ServerPlayer commandPlayer;

        try {
            commandPlayer = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        Team team = teamManager.getTeam(commandPlayer.getUUID());
        if (team == null) {
            commandPlayer.sendSystemMessage(Component.literal("You are not in a team!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        String playerClass = team.getPlayerClass(commandPlayer.getUUID());
        if (!"healer".toUpperCase(Locale.ROOT).equalsIgnoreCase(playerClass)) {
            commandPlayer.sendSystemMessage(Component.literal("Only healers can use this command!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        PlayerUpgrades upgrades = classManager.getUpgrades(commandPlayer.getUUID(),
                PlayerClass.valueOf(playerClass.toUpperCase(Locale.ROOT)));
        int healingPercent = upgrades.getTotalPercent(ClassUpgrade.HEALING);

        float baseHeal = 8.0F;
        float healAmount = baseHeal + (baseHeal * healingPercent / 100.0F);

        for (UUID memberUUID : team.getMembers()) {
            ServerPlayer player = commandPlayer.level().getServer().getPlayerList().getPlayer(memberUUID);
            if (player != null) {
                float newHealth = (float) Math.min(player.getHealth() + healAmount,
                        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).getValue());
                player.setHealth(newHealth);
                player.sendSystemMessage(Component.literal(
                        "You have been healed by your healer for " + healAmount + " HP!")
                        .withStyle(ChatFormatting.GREEN)
                );
            }
        }

        commandPlayer.sendSystemMessage(Component.literal(
                "You healed your team for " + healAmount + " HP each!")
                .withStyle(ChatFormatting.GREEN)
        );
    }
}
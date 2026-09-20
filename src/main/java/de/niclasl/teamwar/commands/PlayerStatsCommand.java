package de.niclasl.teamwar.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.classes.PlayerClass;
import de.niclasl.teamwar.classes.PlayerUpgrades;
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

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class PlayerStatsCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("playerstats")
                        .executes(context -> {
                            playerStats(context.getSource());
                            return 1;
                        })
        );
    }

    private static void playerStats(CommandSourceStack source) {
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

        commandPlayer.sendSystemMessage(Component.literal("=== Player Stats ===")
                .withStyle(ChatFormatting.GOLD));

        for (ServerPlayer player : commandPlayer.level().getServer().getPlayerList().getPlayers()) {
            PlayerClass pc = teamManager.getPlayerClass(player.getUUID());

            PlayerUpgrades pu = (pc != null) ? classManager.getUpgrades(player.getUUID(), pc) : null;

            double maxHealth;
            double damage;
            double defense;

            if (pc != null) {
                maxHealth = pc.getMaxHealth(pu);
                damage = pc.getBaseDamageMultiplier();
                defense = pc.getBaseDefense();
            } else {
                maxHealth = 20.0;
                damage = 1.0;
                defense = 0.0;
            }

            String teamName = teamManager.getTeamOfPlayer(player.getUUID());
            String prefix = "";
            if (teamName != null && teamManager.exists(teamName)) {
                prefix = getTeamPrefix(teamName);
            }

            String stats = String.format("%s%s §7[HP: %.0f | DMG: %.1fx | DEF: %.1f]",
                    prefix, player.getName().getString(), maxHealth, damage, defense);

            commandPlayer.sendSystemMessage(Component.literal(stats));
        }
    }

    private static String getTeamPrefix(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK + "[Black] " + ChatFormatting.RESET;
            case "dark_blue" -> ChatFormatting.DARK_BLUE + "[Dark Blue] " + ChatFormatting.RESET;
            case "dark_green" -> ChatFormatting.DARK_GREEN + "[Dark Green] " + ChatFormatting.RESET;
            case "dark_aqua" -> ChatFormatting.DARK_AQUA + "[Dark Aqua] " + ChatFormatting.RESET;
            case "dark_red" -> ChatFormatting.DARK_RED + "[Dark Red] " + ChatFormatting.RESET;
            case "dark_purple" -> ChatFormatting.DARK_PURPLE + "[Dark Purple] " + ChatFormatting.RESET;
            case "gold" -> ChatFormatting.GOLD + "[Gold] " + ChatFormatting.RESET;
            case "gray" -> ChatFormatting.GRAY + "[Gray] " + ChatFormatting.RESET;
            case "dark_gray" -> ChatFormatting.DARK_GRAY + "[Dark Gray] " + ChatFormatting.RESET;
            case "blue" -> ChatFormatting.BLUE + "[Blue] " + ChatFormatting.RESET;
            case "green" -> ChatFormatting.GREEN + "[Green] " + ChatFormatting.RESET;
            case "aqua" -> ChatFormatting.AQUA + "[Aqua] " + ChatFormatting.RESET;
            case "red" -> ChatFormatting.RED + "[Red] " + ChatFormatting.RESET;
            case "light_purple" -> ChatFormatting.LIGHT_PURPLE + "[Light Purple] " + ChatFormatting.RESET;
            case "yellow" -> ChatFormatting.YELLOW + "[Yellow] " + ChatFormatting.RESET;
            case "white" -> ChatFormatting.WHITE + "[White] " + ChatFormatting.RESET;
            default -> "";
        };
    }
}
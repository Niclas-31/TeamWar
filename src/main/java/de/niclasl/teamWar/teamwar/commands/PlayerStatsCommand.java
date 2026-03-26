package de.niclasl.teamWar.teamwar.commands;

import de.niclasl.teamWar.classes.PlayerClass;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

public class PlayerStatsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        sender.sendMessage(ChatColor.GOLD + "=== Player Stats ===");

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerClass pc = ClassManager.getClass(player);
            PlayerUpgrades pu = (pc != null) ? ClassManager.getUpgrades(player) : null;

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

            String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
            String prefix = "";
            if (teamName != null && TeamManager.exists(teamName)) {
                prefix = getTeamPrefix(teamName);
            }

            String stats = String.format("%s%s §7[HP: %.0f | DMG: %.1fx | DEF: %.1f]",
                    prefix, player.getName(), maxHealth, damage, defense);

            sender.sendMessage(stats);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        return Collections.emptyList();
    }

    private String getTeamPrefix(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "black" -> ChatColor.BLACK + "[Black] " + ChatColor.RESET;
            case "dark_blue" -> ChatColor.DARK_BLUE + "[Dark Blue] " + ChatColor.RESET;
            case "dark_green" -> ChatColor.DARK_GREEN + "[Dark Green] " + ChatColor.RESET;
            case "dark_aqua" -> ChatColor.DARK_AQUA + "[Dark Aqua] " + ChatColor.RESET;
            case "dark_red" -> ChatColor.DARK_RED + "[Dark Red] " + ChatColor.RESET;
            case "dark_purple" -> ChatColor.DARK_PURPLE + "[Dark Purple] " + ChatColor.RESET;
            case "gold" -> ChatColor.GOLD + "[Gold] " + ChatColor.RESET;
            case "gray" -> ChatColor.GRAY + "[Gray] " + ChatColor.RESET;
            case "dark_gray" -> ChatColor.DARK_GRAY + "[Dark Gray] " + ChatColor.RESET;
            case "blue" -> ChatColor.BLUE + "[Blue] " + ChatColor.RESET;
            case "green" -> ChatColor.GREEN + "[Green] " + ChatColor.RESET;
            case "aqua" -> ChatColor.AQUA + "[Aqua] " + ChatColor.RESET;
            case "red" -> ChatColor.RED + "[Red] " + ChatColor.RESET;
            case "light_purple" -> ChatColor.LIGHT_PURPLE + "[Light Purple] " + ChatColor.RESET;
            case "yellow" -> ChatColor.YELLOW + "[Yellow] " + ChatColor.RESET;
            case "white" -> ChatColor.WHITE + "[White] " + ChatColor.RESET;
            default -> "";
        };
    }
}
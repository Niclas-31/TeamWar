package de.niclasl.teamWar.classes.command;

import de.niclasl.teamWar.classes.ClassUpgrade;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HealCommand implements CommandExecutor, TabCompleter {

    public HealCommand() {
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Team team = TeamManager.getTeam(player.getUniqueId());
        if (team == null) {
            player.sendMessage("§cYou are not in a team!");
            return true;
        }

        String playerClass = team.getPlayerClass(player.getUniqueId());
        if (!"healer".equalsIgnoreCase(playerClass)) {
            player.sendMessage("§cOnly healers can use this command!");
            return true;
        }

        PlayerUpgrades upgrades = ClassManager.getUpgrades(player);
        int healingPercent = upgrades.getTotalPercent(ClassUpgrade.HEALING);

        double baseHeal = 8.0;
        double healAmount = baseHeal + (baseHeal * healingPercent / 100.0);

        for (UUID memberUUID : team.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                double newHealth = Math.min(member.getHealth() + healAmount,
                        Objects.requireNonNull(member.getAttribute(Attribute.MAX_HEALTH)).getValue());
                member.setHealth(newHealth);
                member.sendMessage("§aYou have been healed by your healer for " + healAmount + " HP!");
            }
        }

        player.sendMessage("§aYou healed your team for " + healAmount + " HP each!");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        return Collections.emptyList();
    }
}

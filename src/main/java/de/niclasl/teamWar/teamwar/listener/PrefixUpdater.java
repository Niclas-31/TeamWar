package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PrefixUpdater extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String teamName = TeamManager.getPlayerTeams().get(player.getUniqueId());
            String prefix;

            if (player.isOp()) {
                prefix = ChatColor.DARK_RED + "[Admin] " + ChatColor.RESET;
            } else if (teamName != null) {
                prefix = TeamManager.getTeamPrefix(teamName);
            } else {
                prefix = "";
            }

            player.setPlayerListName(prefix + player.getName());
            player.setDisplayName(prefix + player.getName());
        }
    }
}
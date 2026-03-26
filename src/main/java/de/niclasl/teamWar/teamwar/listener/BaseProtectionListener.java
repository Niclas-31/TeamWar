package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class BaseProtectionListener implements Listener {

    public BaseProtectionListener() {
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Team playerTeam = TeamManager.getTeam(uuid);

        if (playerTeam == null) return;

        for (Team team : getAllTeams()) {
            if (team.isInBase(event.getBlock().getLocation())) {
                if (team.equals(playerTeam)) return;

                if (event.getBlock().getType().name().contains("BED")) return;

                event.setCancelled(true);
                player.sendMessage("§cYou are not allowed to mine anything here!");
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Team playerTeam = TeamManager.getTeam(uuid);

        if (playerTeam == null) return;

        for (Team team : getAllTeams()) {
            if (team.isInBase(event.getBlock().getLocation())) {
                if (team.equals(playerTeam)) return;
                event.setCancelled(true);
                player.sendMessage("§cYou are not allowed to build anything here!");
                return;
            }
        }
    }

    public Team[] getAllTeams() {
        return TeamManager.getAllTeams();
    }
}

package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.teamwar.manager.BedManager;
import de.niclasl.teamWar.teamwar.manager.GameStateManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.manager.TeamWarManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Objects;

public class BlockBreakListener implements Listener {

    private static BedManager bedManager;

    public BlockBreakListener(BedManager bedManager) {
        BlockBreakListener.bedManager = bedManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();
        Player player = event.getPlayer();

        if (!type.toString().endsWith("_BED")) return;

        if (GameStateManager.getGameState() != TeamWarManager.GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        if (player.isOp()) {
            event.setCancelled(true);
            return;
        }

        String playerTeam = TeamManager.getTeamOfPlayer(player.getUniqueId());

        for (Team team : TeamManager.getTeams().values()) {
            List<Location> bedParts = bedManager.getTeamBeds().get(team.getName().toLowerCase());
            if (bedParts == null) continue;

            for (Location bedLoc : bedParts) {
                if (bedLoc.getBlockX() == block.getX() &&
                        bedLoc.getBlockY() == block.getY() &&
                        bedLoc.getBlockZ() == block.getZ() &&
                        Objects.equals(bedLoc.getWorld(), block.getWorld())) {

                    String bedTeam = team.getName();

                    if (playerTeam != null && playerTeam.equalsIgnoreCase(bedTeam)) {
                        event.setCancelled(true);
                        player.sendMessage("§cYou can't destroy your own bed!");
                    } else {
                        event.setCancelled(false);
                        Bukkit.broadcastMessage("§cTeam " + bedTeam + "'s bed has been destroyed!");

                        team.destroyBed();
                        bedManager.removeRespawnForTeam(bedTeam);
                        bedManager.removeBed(bedTeam);
                    }
                    return;
                }
            }
        }
    }
}

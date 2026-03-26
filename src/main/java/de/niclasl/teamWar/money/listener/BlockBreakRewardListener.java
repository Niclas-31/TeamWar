package de.niclasl.teamWar.money.listener;

import de.niclasl.teamWar.money.BlockValue;
import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.manager.GameStateManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.manager.TeamWarManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockBreakRewardListener implements Listener {

    public static final Set<Location> brokenBlocks = new HashSet<>();
    public static final Set<Location> placedBlocks = new HashSet<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (GameStateManager.getGameState() == TeamWarManager.GameState.RUNNING) {
            placedBlocks.add(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material blockType = block.getType();

        if (brokenBlocks.contains(loc)) {
            return;
        }

        if (placedBlocks.contains(loc)) {
            placedBlocks.remove(loc);
            return;
        }

        for (Team team : TeamManager.getTeams().values()) {
            if (team.isInBase(loc) && !team.getMembers().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You cannot break blocks in another team's base!");
                event.setCancelled(true);
                return;
            }
        }

        if (!player.isOp()) {
            if (GameStateManager.getGameState() == TeamWarManager.GameState.RUNNING) {
                int reward = BlockValue.getValue(blockType);
                if (reward > 0) {
                    String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
                    if (teamName != null) {
                        MoneyManager.addTeamMoney(teamName, reward);
                        player.sendMessage(ChatColor.GOLD + "You have received " + reward + "$ for mining " + blockType + "!");
                    }
                }

                brokenBlocks.add(loc);
            }
        }
    }

    public static void resetBlocks() {
        brokenBlocks.clear();
        placedBlocks.clear();
    }
}
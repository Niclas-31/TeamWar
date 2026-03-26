package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.teamwar.manager.GameStateManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final GameStateManager gameStateManager;
    private static Map<UUID, Integer> dailyPlaySeconds;
    private final TeamWar plugin;

    public PlayerJoinListener(GameStateManager gameStateManager, Map<UUID, Integer> dailyPlaySeconds, TeamWar plugin) {
        this.gameStateManager = gameStateManager;
        PlayerJoinListener.dailyPlaySeconds = dailyPlaySeconds;
        this.plugin = plugin;
    }

    private static final int MAX_DAILY_SECONDS = 60;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (gameStateManager.isGameRunning()) return;

        int playedSeconds = dailyPlaySeconds.getOrDefault(uuid, 0);
        if (playedSeconds >= MAX_DAILY_SECONDS) {
            Bukkit.getScheduler().runTaskLater(gameStateManager.getPlugin(), () ->
                    player.kickPlayer("§cYou have reached your 2 hours of playtime today!"), 1L);
            return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("§eWeek " + gameStateManager.getCurrentWeek() + "/6  §7Day " + gameStateManager.getCurrentDay() + "/7"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
            Team team = teamName != null ? TeamManager.getTeams().get(teamName.toLowerCase()) : null;

            if (team == null) {
                setSpectator(player);
                Bukkit.broadcastMessage("§c" + player.getName() + " is eliminated! (No team)");
                return;
            }

            if (!team.hasBed()) {
                setSpectator(player);
                Bukkit.broadcastMessage("§c" + player.getName() + " is eliminated! (Bed destroyed)");
                return;
            }

            player.sendMessage("§aYou have respawned again!");
        }, 2L);
    }

    public void setSpectator(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().clear();
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.sendMessage("§7You are eliminated and now a spectator!");
        }, 2L);
    }
}
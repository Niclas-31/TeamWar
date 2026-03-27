package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.playerManagementCore.portal.api.PortalApi;
import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.teamwar.manager.GameStateManager;
import de.niclasl.teamWar.teamwar.manager.LobbyManager;
import de.niclasl.teamWar.teamwar.manager.TeamWarManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location lobby = LobbyManager.getLobbySpawn();

        if (GameStateManager.getGameState() == TeamWarManager.GameState.RUNNING) return;

        if (lobby == null) {
            player.sendMessage("§cLobby spawn is not set!");
            return;
        }

        Bukkit.getScheduler().runTaskLater(TeamWar.getInstance(), () -> {
            if (player.isOnline()) {
                PortalApi.markTeleport(player, "TeamWar");
                player.teleport(lobby);
            }
        }, 1L);
    }
}

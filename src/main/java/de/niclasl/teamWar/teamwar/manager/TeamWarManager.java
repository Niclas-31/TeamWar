package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;

public record TeamWarManager(TeamWar plugin) {

    public enum GameState {
        LOBBY,
        RUNNING,
        ENDED
    }

    public void load() {
        TeamManager.loadAllTeams();
        plugin.getLobbyManager().loadLobbySpawn();
        plugin.getLobbyManager().loadLobbyPlayers();
    }
}
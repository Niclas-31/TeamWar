package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.teamwar.manager.TeamWarManager.GameState;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GameStateManager {

    private static GameState gameState = GameState.LOBBY;
    private static int currentDay = 1;
    private static int currentWeek = 1;
    private static final int DAYS_PER_WEEK = 7;

    private static TeamWar plugin;

    public GameStateManager(TeamWar plugin) {
        GameStateManager.plugin = plugin;
        loadGameState();
        startDayCycle();
    }

    public void startDayCycle() {
        long ticksPerDay = 20L * 60L * 60L * 24L;

        Bukkit.getScheduler().runTaskTimer(plugin, GameStateManager::startNewDay, ticksPerDay, ticksPerDay);
    }

    public static void startNewDay() {
        currentDay++;

        if (currentDay > DAYS_PER_WEEK) {
            nextWeek();
            currentDay = 1;
        }

        PlayerTimeManager.resetDailyTimes();

        plugin.getServer().broadcastMessage(
                "§eIt is now day " + currentDay + " of week " + currentWeek + "."
        );
    }

    public static void nextWeek() {
        currentWeek++;
        plugin.getLogger().info("Week advanced to " + currentWeek);
        Bukkit.broadcastMessage("§eWeek " + currentWeek + " has started!");
    }

    public static GameState getGameState() { return gameState; }

    public void setGameState(GameState newState) {
        gameState = newState;
        saveGameState();

        if (newState == GameState.RUNNING) {
            PlayerTimeManager.startDailyTimer();
        } else {
            PlayerTimeManager.stopDailyTimer();
            PlayerTimeManager.resetDailyTimes();
        }
    }

    private void saveGameState() {
        plugin.getConfig().set("gameState", gameState.name());
        plugin.getConfig().set("currentWeek", currentWeek);
        plugin.getConfig().set("currentDay", currentDay);
        plugin.saveConfig();
    }

    private void loadGameState() {
        String stateName = plugin.getConfig().getString("gameState", GameState.LOBBY.name());
        try {
            gameState = GameState.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            gameState = GameState.LOBBY;
        }

        currentWeek = plugin.getConfig().getInt("currentWeek", 1);
        currentDay = plugin.getConfig().getInt("currentDay", 1);
    }

    public int getCurrentWeek() { return currentWeek; }
    public int getCurrentDay() { return currentDay; }
    public boolean isGameRunning() { return gameState == GameState.RUNNING; }
    public Plugin getPlugin() { return plugin; }
}
package de.niclasl.teamwar.game;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.player.PlayerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static GameState gameState = GameState.LOBBY;
    private static int currentDay = 1;
    private static int currentWeek = 1;
    private static final int DAYS_PER_WEEK = 7;

    private final MinecraftServer server;
    private final PlayerManager playerManager;

    private final GameSavedData data;

    public GameManager(MinecraftServer server) {
        this.server = server;
        this.playerManager = TeamWar.getPlayerManager();

        this.data = GameSavedData.get(server.overworld());

        loadGameState();
        startDayCycle();
    }

    public void startDayCycle() {
        long ticksPerDay = 20L * 60L * 60L * 24L;
        long secondsPerDay = ticksPerDay / 20L;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                () -> server.execute(this::startNewDay),
                secondsPerDay,
                secondsPerDay,
                TimeUnit.SECONDS
        );
    }

    public void startNewDay() {
        currentDay++;

        if (currentDay > DAYS_PER_WEEK) {
            nextWeek();
            currentDay = 1;
        }

        playerManager.resetDailyTimes();

        server.getPlayerList().broadcastSystemMessage(
                Component.literal(
                        "It is now day " + currentDay +
                                " of week " + currentWeek + "."
                ).withStyle(ChatFormatting.YELLOW),
                false
        );
    }

    public void nextWeek() {
        currentWeek++;
        TeamWar.LOGGER.info("Week advanced to {}", currentWeek);

        server.getPlayerList().broadcastSystemMessage(
                Component.literal(
                        "Week " + currentWeek + "has started!"
                ).withStyle(ChatFormatting.YELLOW),
                false
        );
    }

    public static GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState newState) {
        gameState = newState;
        saveGameState();

        if (newState == GameState.RUNNING) {
            playerManager.startDailyTimer();
        } else {
            PlayerManager.stopDailyTimer();
            playerManager.resetDailyTimes();
        }
    }

    private void saveGameState() {
        data.gameState = gameState;

        data.setDirty();
    }

    private void loadGameState() {
        gameState = data.gameState;
    }
}
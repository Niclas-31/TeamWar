package de.niclasl.teamwar.player;

import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerManager {

    private static final int MAX_SESSION_SECONDS = 7200;

    private final MinecraftServer server;
    private final ScheduledExecutorService scheduler;
    private final PlayerSavedData data;

    private static ScheduledFuture<?> timerTask;

    private final Map<UUID, Integer> dailyPlaySeconds = new HashMap<>();
    private final Map<UUID, Long> joinTime = new HashMap<>();

    private final int maxDailySeconds;

    public PlayerManager(MinecraftServer server, int maxDailySeconds) {
        this.server = server;
        this.maxDailySeconds = maxDailySeconds;
        this.data = server.getDataStorage().computeIfAbsent(PlayerSavedData.TYPE);

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startDailyTimer() {
        if (timerTask == null || timerTask.isCancelled()) {
            timerTask = scheduler.scheduleAtFixedRate(
                    () -> server.execute(this::tick),
                    1,
                    1,
                    TimeUnit.SECONDS
            );
        }
    }

    private void tick() {
        if (GameManager.getGameState() == GameState.LOBBY || GameManager.getGameState() == GameState.ENDED) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (server.getPlayerList().isOp(player.nameAndId())) {
                continue;
            }

            UUID uuid = player.getUUID();
            int played = dailyPlaySeconds.getOrDefault(uuid, 0);

            if (played >= maxDailySeconds) {
                saveTime(uuid);
                player.connection.disconnect(Component.translatable( "message.playtime.is_reached"));
                continue;
            }

            long sessionSeconds =
                    (System.currentTimeMillis()
                            - joinTime.getOrDefault(
                                    uuid,
                            System.currentTimeMillis()
                    )) / 1000L;

            sessionSeconds = Math.min(sessionSeconds, MAX_SESSION_SECONDS);

            int currentPlayTime = played + (int) sessionSeconds;

            currentPlayTime = Math.min(currentPlayTime, maxDailySeconds);
            dailyPlaySeconds.put(uuid, currentPlayTime);
            int remaining = Math.max(0, maxDailySeconds - currentPlayTime);

            player.sendSystemMessage(
                    Component.translatable("message.playtime.daily",
                            formatTime(remaining)), true);

            if (currentPlayTime > 0 && currentPlayTime % 60 == 0) {
                saveTime(uuid);
            }
        }
    }

    public void playerJoin(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (server.getPlayerList().isOp(player.nameAndId())) return;

        int played = data.totalTime.getOrDefault(uuid, 0);
        dailyPlaySeconds.put(uuid, played);
        joinTime.put(uuid, System.currentTimeMillis());

        GameState newState = GameManager.getGameState();

        if (newState == GameState.RUNNING) {
            startDailyTimer();
        }
    }

    public void playerQuit(ServerPlayer player) {
        UUID uuid = player.getUUID();

        GameState newState = GameManager.getGameState();

        if (newState == GameState.RUNNING) {
            if (!server.getPlayerList().isOp(player.nameAndId())) {
                saveTime(uuid);
            }
        }
    }

    public void saveTime(UUID uuid) {
        Long start = joinTime.get(uuid);

        if (start == null) {
            return;
        }

        long sessionSeconds = (System.currentTimeMillis() - start) / 1000;

        sessionSeconds = Math.min(sessionSeconds, MAX_SESSION_SECONDS);

        int total = dailyPlaySeconds.getOrDefault(uuid, 0) + (int) sessionSeconds;

        total = Math.min(total, maxDailySeconds);

        dailyPlaySeconds.put(uuid, total);
        data.totalTime.put(uuid, total);

        data.setDirty();
    }

    public void resetDailyTimes() {
        dailyPlaySeconds.replaceAll((_, _) -> 0);
        data.totalTime.replaceAll((_, _) -> 0);

        data.setDirty();
    }

    private static String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static void stopDailyTimer() {
        if (timerTask != null && !timerTask.isCancelled()) timerTask.cancel(true);
    }

    public int getDailyPlayTime(UUID uuid) {
        return dailyPlaySeconds.getOrDefault(uuid, 0);
    }
}
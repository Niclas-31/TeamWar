package de.niclasl.teamwar.pvp;

import de.niclasl.teamwar.game.GameSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PvPManager {

    private final int maxWeeks = 6;

    private final MinecraftServer server;
    private final ScheduledExecutorService scheduler;
    private final GameSavedData data;

    private ScheduledFuture<?> weekTask;

    public PvPManager(MinecraftServer server) {
        this.server = server;
        this.data = GameSavedData.get(server.overworld());

        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        startWeekCounter();
    }

    public void startGame() {
        data.gameStartTime = System.currentTimeMillis();

        data.currentDay = 1;
        data.currentWeek = 1;

        data.setDirty();

        startWeekCounter();
    }

    public void resetGame() {
        stopWeekCounter();

        data.gameStartTime = 0L;
        data.currentWeek = 1;
        data.currentDay = 1;

        data.setDirty();
    }

    public void startWeekCounter() {
        if (data.gameStartTime == 0L) {
            return;
        }

        weekTask = scheduler.scheduleAtFixedRate(
                this::updateWeek,
                1,
                1,
                TimeUnit.MINUTES
        );
    }

    private void updateWeek() {
        server.execute(() -> {

            if (data.gameStartTime == 0L) {
                return;
            }

            long elapsedMs = System.currentTimeMillis() - data.gameStartTime;

            long elapsedDays = elapsedMs / (1000L * 60L * 60L * 24L);

            int oldWeek = data.currentWeek;

            data.currentWeek = (int) (elapsedDays / 7) + 1;

            data.currentDay = (int) (elapsedDays % 7) + 1;

            if (data.currentWeek != oldWeek) {
                data.setDirty();
            }

            if (data.currentWeek >= maxWeeks) {

                broadcast(Component.translatable("message.pvp.is_now_enabled")
                        .withStyle(ChatFormatting.RED));

                stopWeekCounter();
            }
        });
    }

    public void stopWeekCounter() {
        if (weekTask != null) {
            weekTask.cancel(false);
            weekTask = null;
        }
    }

    public int getCurrentWeek() {
        return data.currentWeek;
    }

    public int getCurrentDay() {
        return data.currentDay;
    }

    public boolean isPvpAllowed() {
        if (data.gameStartTime == 0L) {
            return false;
        }
        return data.currentWeek >= maxWeeks;
    }

    public long getMillisUntilPvp() {

        if (data.gameStartTime == 0L) {
            return -1L;
        }

        long protectionMillis = maxWeeks * 7L * 24L * 60L * 60L * 1000L;

        long elapsed = System.currentTimeMillis() - data.gameStartTime;

        return Math.max(0L, protectionMillis - elapsed);
    }

    public int getMaxWeeks() {
        return maxWeeks;
    }

    public String getFormattedTimeUntilPvp() {

        long millis = getMillisUntilPvp();

        if (millis <= 0) return Component.translatable("message.pvp.is_enabled").getString();

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;

        return days + "d " + hours + "h " + minutes + "m";
    }

    private void broadcast(Component message) {
        server.getPlayerList().broadcastSystemMessage(
                message,
                false
        );
    }
}
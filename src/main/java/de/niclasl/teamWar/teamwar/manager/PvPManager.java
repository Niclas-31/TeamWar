package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PvPManager {

    private static TeamWar plugin;

    private static int maxWeeks;
    private static long gameStartTime;

    private static BukkitTask weekTask;

    private static int currentWeek = 1;
    private static int currentDay = 1;

    public static void init(TeamWar pluginInstance) {
        plugin = pluginInstance;
        maxWeeks = 6;

        loadStartTime();
        startWeekCounter();
    }

    public static void startGame() {
        gameStartTime = System.currentTimeMillis();

        plugin.getConfig().set("gameStartTime", gameStartTime);
        plugin.saveConfig();

        startWeekCounter();
    }

    public static void resetGame() {

        stopWeekCounter();

        gameStartTime = 0;
        currentWeek = 1;
        currentDay = 1;

        plugin.getConfig().set("gameStartTime", null);
        plugin.saveConfig();
    }

    private static void loadStartTime() {
        gameStartTime = plugin.getConfig().getLong("gameStartTime", 0L);
    }

    public static void startWeekCounter() {

        if (gameStartTime == 0) return;

        if (weekTask != null) {
            weekTask.cancel();
        }

        weekTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long elapsedMs = System.currentTimeMillis() - gameStartTime;

            long elapsedDays = elapsedMs / (1000 * 60 * 60 * 24);

            currentWeek = (int) (elapsedDays / 7) + 1;
            currentDay = (int) (elapsedDays % 7) + 1;

            if (currentWeek >= maxWeeks) {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.RED + "PvP is now enabled!");
                }

                stopWeekCounter();
            }

        }, 20L, 20L * 60);

    }

    public static void stopWeekCounter() {
        if (weekTask != null) {
            weekTask.cancel();
            weekTask = null;
        }
    }

    public static int getCurrentWeek() {
        return currentWeek;
    }

    public static int getCurrentDay() {
        return currentDay;
    }

    public static boolean isPvpAllowed() {
        if (gameStartTime == 0) return false;
        return currentWeek >= maxWeeks;
    }

    public static long getMillisUntilPvp() {

        if (gameStartTime == 0) return -1;

        long protectionMillis = maxWeeks * 7L * 24 * 60 * 60 * 1000;

        long elapsed = System.currentTimeMillis() - gameStartTime;

        return Math.max(0, protectionMillis - elapsed);
    }

    public static int getMaxWeeks() {
        return maxWeeks;
    }

    public static String getFormattedTimeUntilPvp() {

        long millis = getMillisUntilPvp();

        if (millis <= 0) return "PvP enabled";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;

        return days + "d " + hours + "h " + minutes + "m";
    }
}
package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTimeManager implements Listener {

    private static final Map<UUID, Integer> dailyPlaySeconds = new HashMap<>();
    private static final Map<UUID, Long> joinTime = new HashMap<>();
    private static int maxDailySeconds;
    private static TeamWar plugin;
    private static BukkitTask timerTask;
    private static final int MAX_SESSION_SECONDS = 7200;

    public PlayerTimeManager(int maxDailySeconds, TeamWar plugin) {
        PlayerTimeManager.maxDailySeconds = maxDailySeconds;
        PlayerTimeManager.plugin = plugin;
    }

    public static void startDailyTimer() {
        if (timerTask == null || timerTask.isCancelled()) {
            timerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (GameStateManager.getGameState() == TeamWarManager.GameState.LOBBY ||
                            GameStateManager.getGameState() == TeamWarManager.GameState.ENDED) {
                        return;
                    }

                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player.isOp()) continue;

                        UUID uuid = player.getUniqueId();
                        int played = dailyPlaySeconds.getOrDefault(uuid, 0);

                        if (played >= maxDailySeconds) {
                            saveTime(uuid);
                            player.kickPlayer("§cYou have reached your daily playtime limit!");
                            continue;
                        }

                        long sessionSeconds = (System.currentTimeMillis() - joinTime.getOrDefault(uuid, System.currentTimeMillis())) / 1000;
                        int addSeconds = Math.min(1, MAX_SESSION_SECONDS - (int) sessionSeconds);
                        dailyPlaySeconds.put(uuid, played + addSeconds);

                        player.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                new TextComponent(
                                        "§eDaily Playtime left: " + formatTime(Math.max(0, maxDailySeconds - played))
                                )
                        );

                        if ((played + addSeconds) % 60 == 0) saveTime(uuid);
                    }
                }
            }.runTaskTimer(plugin, 20, 20);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.isOp()) return;

        int played = plugin.getConfig().getInt("playtime." + uuid, 0);
        dailyPlaySeconds.put(uuid, played);
        joinTime.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!player.isOp()) saveTime(uuid);
    }

    public static void saveTime(UUID uuid) {
        Long start = joinTime.get(uuid);
        if (start == null) return;

        long sessionSeconds = (System.currentTimeMillis() - start) / 1000;
        sessionSeconds = Math.min(sessionSeconds, MAX_SESSION_SECONDS);

        int total = dailyPlaySeconds.getOrDefault(uuid, 0) + (int) sessionSeconds;
        dailyPlaySeconds.put(uuid, total);

        plugin.getConfig().set("playtime." + uuid, total);
        plugin.saveConfig();
    }

    public static void resetDailyTimes() {
        dailyPlaySeconds.clear();
        plugin.getConfig().set("playtime", 0);
        plugin.saveConfig();
    }

    private static String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static void stopDailyTimer() {
        if (timerTask != null && !timerTask.isCancelled()) timerTask.cancel();
    }

    public static int getDailyPlayTime(UUID uuid) {
        return dailyPlaySeconds.getOrDefault(uuid, 0);
    }
}
package de.niclasl.teamWar.teamwar.manager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BorderManager {

    private final JavaPlugin plugin;

    private BukkitTask task;

    public BorderManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTask() {

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            int day = PvPManager.getCurrentDay();
            int week = PvPManager.getCurrentWeek();

            World world = Bukkit.getWorlds().getFirst();
            WorldBorder border = world.getWorldBorder();

            if (week == 1 && day == 5) {
                border.setSize(15000000);
            } else if (week == 2 && day == 3) {
                border.setSize(5000000);
            } else if (week == 3 && day == 1) {
                border.setSize(900000);
            } else if (week == 3 && day == 6) {
                border.setSize(700000);
            } else if (week == 4 && day == 4) {
                border.setSize(500000);
            } else if (week == 5 && day == 2) {
                border.setSize(50000);
            } else if (week == 5 && day == 7) {
                border.setSize(25000);
            } else if (week == 6 && day == 5) {
                border.setSize(5000);
            }

        }, 0L, 20L * 60);
    }

    public void stopGameBorder() {
        if (task != null) task.cancel();

        World world = Bukkit.getWorlds().getFirst();

        world.getWorldBorder().setSize(35);
    }

    public void startGameBorder() {
        startTask();

        World world = Bukkit.getWorlds().getFirst();

        world.getWorldBorder().setSize(30000000);
    }
}
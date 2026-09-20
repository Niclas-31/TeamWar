package de.niclasl.teamwar.border;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BorderManager {

    private final MinecraftServer server;

    private ScheduledFuture<?> task;
    private final ScheduledExecutorService scheduler;

    public BorderManager(MinecraftServer server) {
        this.server = server;

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startTask() {

        stopTask();

        task = scheduler.scheduleAtFixedRate(
                () -> server.execute(() -> {

                    int day = TeamWar.getPvPManager().getCurrentDay();
                    int week = TeamWar.getPvPManager().getCurrentWeek();

                    ServerLevel level = server.overworld();
                    WorldBorder border = level.getWorldBorder();

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

                }),
                0,
                1,
                TimeUnit.MINUTES
        );
    }

    public void stopTask() {

        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    public void stopGameBorder() {

        stopTask();

        ServerLevel level = server.overworld();
        level.getWorldBorder().setSize(36);
    }

    public void startGameBorder() {

        startTask();

        ServerLevel level = server.overworld();
        level.getWorldBorder().setSize(30000000);
    }
}
package de.niclasl.teamwar.repair;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class WorldRepairManager {

    private static boolean running = false;
    private static int ticks = 0;

    private static final int REPAIR_DURATION = 1200;
    private static boolean errorTriggered = false;

    public static void start(ServerLevel level) {
        if (running) {
            return;
        }

        WorldBalanceSavedData balance = level.getServer().getDataStorage().computeIfAbsent(WorldBalanceSavedData.TYPE);

        if (balance.getState() != WorldBalanceState.BALANCED) {
            return;
        }

        balance.setState(WorldBalanceState.UNSTABLE);

        balance.setDirty();

        running = true;
        ticks = 0;
        errorTriggered = false;
    }

    public static void tick(ServerLevel level) throws IOException {
        if (!running) {
            return;
        }

        ticks++;

        int progress = Math.min(100, ticks * 100 / REPAIR_DURATION);

        if (progress >= 83 && !errorTriggered) {
            errorTriggered = true;

            WorldBalanceSavedData balance = level.getServer().getDataStorage().computeIfAbsent(WorldBalanceSavedData.TYPE);

            balance.setState(WorldBalanceState.INCOMPATIBLE);

            balance.setDirty();

            Path overworldPath = level.getServer().getWorldPath(LevelResource.ROOT);

            Path repairFile = level.getServer().getServerDirectory()
                    .resolve("config")
                    .resolve(TeamWar.MOD_ID)
                    .resolve("overworld_repair.dat");

            UUID currentIdentity = getOrCreateWorldIdentity(overworldPath);

            Files.createDirectories(repairFile.getParent());
            Files.writeString(repairFile, currentIdentity.toString());

            level.getServer().halt(false);

            running = false;
        }
    }

    private static UUID getOrCreateWorldIdentity(Path overworldPath) throws IOException {

        Path identityPath = overworldPath
                .resolve("dimensions")
                .resolve("minecraft")
                .resolve("overworld")
                .resolve("world_identity.dat");

        if (Files.exists(identityPath)) {
            String value = Files.readString(identityPath).trim();
            return UUID.fromString(value);
        }

        UUID newIdentity = UUID.randomUUID();

        Files.writeString(
                identityPath,
                newIdentity.toString(),
                StandardOpenOption.CREATE_NEW
        );

        return newIdentity;
    }
}
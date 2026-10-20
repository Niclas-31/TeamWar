package de.niclasl.teamwar.classes;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class ClassManager {
    private final PlayerUpgradeSavedData data;

    public ClassManager(MinecraftServer server) {
        this.data = server.getDataStorage().computeIfAbsent(PlayerUpgradeSavedData.TYPE);
    }

    public PlayerUpgrades getUpgrades(UUID uuid, PlayerClass playerClass) {

        PlayerUpgrades playerUpgrades = this.data.playerUpgrades.get(uuid);

        if (playerUpgrades == null) {
            playerUpgrades = createPlayer(uuid, playerClass);
        }

        return playerUpgrades;
    }

    public PlayerUpgrades createPlayer(UUID uuid, PlayerClass playerClass) {
        PlayerUpgrades playerUpgrades = new PlayerUpgrades(uuid);

        for (ClassUpgrade upgrade : playerClass.getAvailableUpgrades()) {

            playerUpgrades.setLevel(upgrade, 0);
        }

        data.playerUpgrades.put(uuid, playerUpgrades);

        data.setDirty();

        return playerUpgrades;
    }

    public void upgradePlayer(UUID uuid, ClassUpgrade upgrade) {
        PlayerUpgrades playerUpgrades = data.playerUpgrades.get(uuid);

        if (playerUpgrades == null) {
            return;
        }

        playerUpgrades.upgrade(upgrade);

        data.playerUpgrades.put(uuid, playerUpgrades);

        data.setDirty();
    }

    public PlayerClass getClass(UUID uuid) {
        return TeamWar.getTeamManager().getPlayerClass(uuid);
    }
}
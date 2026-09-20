package de.niclasl.teamwar.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerUpgrades {
    private final UUID playerId;
    private final Map<ClassUpgrade, Integer> upgrades = new HashMap<>();

    public PlayerUpgrades(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Map<ClassUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    public int getLevel(ClassUpgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public void setLevel(ClassUpgrade upgrade, int level) {
        upgrades.put(upgrade, level);
    }

    public void upgrade(ClassUpgrade upgrade) {
        setLevel(upgrade, getLevel(upgrade) + 1);
    }

    public int getTotalPercent(ClassUpgrade upgrade) {
        return getLevel(upgrade) * upgrade.getPercentPerLevel();
    }
}
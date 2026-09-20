package de.niclasl.teamwar.classes;

import java.util.List;

public enum PlayerClass {
    MINER(20, 1.0, 0.0, List.of(ClassUpgrade.SPEED, ClassUpgrade.HEARTS)),
    TANK(40, 1.2, 0.2, List.of(ClassUpgrade.HEARTS, ClassUpgrade.DAMAGE, ClassUpgrade.DEFENSE)),
    HEALER(20, 0.8, 0.1, List.of(ClassUpgrade.HEALING, ClassUpgrade.DEFENSE)),
    ARCHER(20, 1.5, 0.0, List.of(ClassUpgrade.DAMAGE, ClassUpgrade.HEARTS, ClassUpgrade.DEFENSE)),
    BUILDER(24, 1.0, 0.1, List.of(ClassUpgrade.HEARTS, ClassUpgrade.DEFENSE));

    private final float baseHealth;
    private final double baseDamageMultiplier;
    private final double baseDefense;
    private final List<ClassUpgrade> availableUpgrades;

    PlayerClass(float baseHealth, double baseDamageMultiplier, double baseDefense, List<ClassUpgrade> availableUpgrades) {
        this.baseHealth = baseHealth;
        this.baseDamageMultiplier = baseDamageMultiplier;
        this.baseDefense = baseDefense;
        this.availableUpgrades = availableUpgrades;
    }

    public double getBaseDamageMultiplier() {
        return baseDamageMultiplier;
    }

    public double getBaseDefense() {
        return baseDefense;
    }

    public List<ClassUpgrade> getAvailableUpgrades() {
        return availableUpgrades;
    }

    public float getMaxHealth(PlayerUpgrades playerUpgrades) {
        int heartLevel = playerUpgrades.getLevel(ClassUpgrade.HEARTS);
        float percent = heartLevel * ClassUpgrade.HEARTS.getPercentPerLevel();
        return baseHealth + (baseHealth * percent / 100.0F);
    }
}
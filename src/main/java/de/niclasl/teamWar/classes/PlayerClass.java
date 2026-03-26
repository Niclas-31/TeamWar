package de.niclasl.teamWar.classes;

import de.niclasl.teamWar.classes.manager.PlayerUpgrades;

public enum PlayerClass {
    MINER(20, 1.0, 0.0, new ClassUpgrade[]{ClassUpgrade.SPEED, ClassUpgrade.HEARTS}),
    TANK(40, 1.2, 0.2, new ClassUpgrade[]{ClassUpgrade.HEARTS, ClassUpgrade.DAMAGE, ClassUpgrade.DEFENSE}),
    HEALER(20, 0.8, 0.1, new ClassUpgrade[]{ClassUpgrade.HEALING, ClassUpgrade.DEFENSE}),
    ARCHER(20, 1.5, 0.0, new ClassUpgrade[]{ClassUpgrade.DAMAGE, ClassUpgrade.HEARTS, ClassUpgrade.DEFENSE}),
    BUILDER(24, 1.0, 0.1, new ClassUpgrade[]{ClassUpgrade.HEARTS, ClassUpgrade.DEFENSE});

    private final double baseHealth;
    private final double baseDamageMultiplier;
    private final double baseDefense;
    private final ClassUpgrade[] availableUpgrades;

    PlayerClass(double baseHealth, double baseDamageMultiplier, double baseDefense, ClassUpgrade[] availableUpgrades) {
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

    public ClassUpgrade[] getAvailableUpgrades() {
        return availableUpgrades;
    }

    public double getMaxHealth(PlayerUpgrades playerUpgrades) {
        int heartLevel = playerUpgrades.getLevel(ClassUpgrade.HEARTS);
        double percent = heartLevel * ClassUpgrade.HEARTS.getPercentPerLevel();
        return baseHealth + (baseHealth * percent / 100.0);
    }
}
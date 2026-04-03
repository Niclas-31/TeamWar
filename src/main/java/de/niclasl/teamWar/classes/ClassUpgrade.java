package de.niclasl.teamWar.classes;

public enum ClassUpgrade {
    HEARTS("§cHearts", 2, 50),
    DEFENSE("§aDefense", 5, 25),
    DAMAGE("§4Damage", 10, 75),
    SPEED("§bSpeed", 10, 40),
    HEALING("§dHealing", 10, 50);

    private final String displayName;
    private final int percentPerLevel;
    private final int baseCost;

    ClassUpgrade(String displayName, int percentPerLevel, int baseCost) {
        this.displayName = displayName;
        this.percentPerLevel = percentPerLevel;
        this.baseCost = baseCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPercentPerLevel() {
        return percentPerLevel;
    }

    public int getCost(int level) {
        return baseCost * (level + 1);
    }
}
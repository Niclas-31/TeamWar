package de.niclasl.teamWar.classes;

public enum ClassUpgrade {
    HEARTS("§cHearts", 2),
    DEFENSE("§aDefense", 5),
    DAMAGE("§4Damage", 10),
    SPEED("§bDegradation speed", 10),
    HEALING("§dCure", 10);

    private final String displayName;
    private final int percentPerLevel;

    ClassUpgrade(String displayName, int percentPerLevel) {
        this.displayName = displayName;
        this.percentPerLevel = percentPerLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPercentPerLevel() {
        return percentPerLevel;
    }

    public int getCost(int level) {
        return 5 * (level + 1);
    }
}

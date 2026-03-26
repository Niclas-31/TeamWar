package de.niclasl.teamWar.money;

public class ValueRange {
    private final int min;
    private final int max;

    public ValueRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getRandom() {
        if (min == max) return min;
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}

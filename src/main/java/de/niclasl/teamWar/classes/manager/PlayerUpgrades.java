package de.niclasl.teamWar.classes.manager;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.classes.ClassUpgrade;
import de.niclasl.teamWar.classes.PlayerClass;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerUpgrades {
    private final PlayerClass classType;
    private final UUID playerId;
    private final Map<ClassUpgrade, Integer> upgrades = new HashMap<>();
    private static final FileConfiguration cfg = TeamWar.getInstance().getConfig();
    private static final String PREFIX = "players.";

    public PlayerUpgrades(UUID playerId, PlayerClass classType) {
        this.playerId = playerId;
        this.classType = classType;
        for (ClassUpgrade upgrade : classType.getAvailableUpgrades()) {
            upgrades.put(upgrade, 0);
        }
    }

    public PlayerClass getClassType() {
        return classType;
    }

    public int getLevel(ClassUpgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public void upgrade(ClassUpgrade upgrade) {
        upgrades.put(upgrade, getLevel(upgrade) + 1);
    }

    public int getTotalPercent(ClassUpgrade upgrade) {
        return getLevel(upgrade) * upgrade.getPercentPerLevel();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public static void saveUpgrades(PlayerUpgrades upgrades) {
        UUID id = upgrades.getPlayerId();
        cfg.set(PREFIX + id + ".class", upgrades.getClassType().name());

        for (ClassUpgrade upgrade : upgrades.getClassType().getAvailableUpgrades()) {
            cfg.set(PREFIX + id + ".upgrades." + upgrade.name(), upgrades.getLevel(upgrade));
        }

        TeamWar.getInstance().saveConfig();
    }

    public static void loadUpgrades() {
        FileConfiguration cfg = TeamWar.getInstance().getConfig();

        if (!cfg.isConfigurationSection("players")) return;

        for (String uuidStr : Objects.requireNonNull(cfg.getConfigurationSection("players")).getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuidStr);
                String className = cfg.getString("players." + uuidStr + ".class");
                if (className == null) continue;

                PlayerClass pc = PlayerClass.valueOf(className);
                PlayerUpgrades upgrades = new PlayerUpgrades(playerId, pc);

                if (cfg.isConfigurationSection("players." + uuidStr + ".upgrades")) {
                    for (String upgradeName : Objects.requireNonNull(cfg.getConfigurationSection("players." + uuidStr + ".upgrades")).getKeys(false)) {
                        ClassUpgrade upgrade = ClassUpgrade.valueOf(upgradeName);
                        int level = cfg.getInt("players." + uuidStr + ".upgrades." + upgradeName, 0);
                        for (int i = 0; i < level; i++) upgrades.upgrade(upgrade);
                    }
                }

                ClassManager.getPlayerUpgradesMap().put(playerId, upgrades);

            } catch (IllegalArgumentException ignored) {}
        }
    }
}

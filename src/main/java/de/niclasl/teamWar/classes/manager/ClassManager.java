package de.niclasl.teamWar.classes.manager;

import de.niclasl.teamWar.classes.PlayerClass;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClassManager {
    private static final HashMap<UUID, PlayerClass> playerClasses = new HashMap<>();
    private static final HashMap<UUID, PlayerUpgrades> playerUpgrades = new HashMap<>();

    public void setClass(Player player, PlayerClass playerClass) {
        UUID id = player.getUniqueId();
        playerClasses.put(id, playerClass);

        PlayerUpgrades up = getUpgrades(player);
        double maxHealth = playerClass.getMaxHealth(up);

        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH))
                .setBaseValue(maxHealth);
        player.setHealth(maxHealth);
    }

    public static void setClass(UUID uuid, PlayerClass playerClass) {
        playerClasses.put(uuid, playerClass);
        playerUpgrades.putIfAbsent(uuid, new PlayerUpgrades(uuid, playerClass));
    }

    public static PlayerClass getClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public static PlayerUpgrades getUpgrades(Player player) {
        UUID id = player.getUniqueId();
        if (!playerUpgrades.containsKey(id)) {
            PlayerClass pc = getClass(player);
            if (pc != null) {
                playerUpgrades.put(id, new PlayerUpgrades(id, pc));
            }
        }
        return playerUpgrades.get(id);
    }

    public static Map<UUID, PlayerUpgrades> getPlayerUpgradesMap() {
        return playerUpgrades;
    }
}
package de.niclasl.teamWar.classes.gui;

import de.niclasl.teamWar.classes.ClassUpgrade;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class UpgradeGUI {

    public static void open(Player player, PlayerUpgrades playerUpgrades) {
        if (playerUpgrades == null) {
            player.sendMessage("§cYou haven't chosen a class yet!");
            return;
        }

        var playerClass = playerUpgrades.getClassType();
        List<ClassUpgrade> available = List.of(playerClass.getAvailableUpgrades());

        Inventory inv = Bukkit.createInventory(null, 9, "§6Upgrades: " + playerClass.name());

        for (int i = 0; i < available.size(); i++) {
            ClassUpgrade upgrade = available.get(i);
            int level = playerUpgrades.getLevel(upgrade);
            int percent = playerUpgrades.getTotalPercent(upgrade);
            int cost = upgrade.getCost(level);

            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(upgrade.getDisplayName() + " §7(" + percent + "%)");
            meta.setLore(Arrays.asList(
                    "§7Next upgrade: +" + upgrade.getPercentPerLevel() + "%",
                    "§7Cost: " + cost + " Level",
                    "§7Current level: " + level
            ));

            meta.getPersistentDataContainer().set(
                    new NamespacedKey("teamwar", "upgrade"),
                    PersistentDataType.STRING,
                    upgrade.name()
            );

            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }
}
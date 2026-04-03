package de.niclasl.teamWar.classes.listener;

import de.niclasl.teamWar.classes.ClassUpgrade;
import de.niclasl.teamWar.classes.gui.UpgradeGUI;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.classes.PlayerClass;
import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.attribute.Attribute;

public class UpgradeGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!event.getView().getTitle().startsWith("§6Upgrades: ")) return;

        event.setCancelled(true);

        Team team = TeamManager.getTeam(player.getUniqueId());
        String teamName;
        try {
            assert team != null;
            teamName = team.getName();
        } catch (NullPointerException ignored) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        assert meta != null;
        String upgradeKey = meta.getPersistentDataContainer().get(
                new NamespacedKey("teamwar", "upgrade"),
                PersistentDataType.STRING
        );
        if (upgradeKey == null) return;

        PlayerUpgrades upgrades = ClassManager.getUpgrades(player);
        if (upgrades == null) return;

        ClassUpgrade upgrade = ClassUpgrade.valueOf(upgradeKey);

        int level = upgrades.getLevel(upgrade);
        int cost = upgrade.getCost(level);
        int totalPercent = upgrades.getTotalPercent(upgrade);

        if (totalPercent >= 100) {
            player.sendMessage("§cThis upgrade is already at maximum (100%)!");
            return;
        }

        double dollar = MoneyManager.getTeamMoney(teamName);
        if (dollar < cost) {
            player.sendMessage("§cYou need " + cost + " dollar to upgrade!");
            return;
        }

        MoneyManager.setTeamMoney(teamName, dollar - cost);

        upgrades.upgrade(upgrade);
        PlayerUpgrades.saveUpgrades(upgrades);

        PlayerClass playerClass = upgrades.getClassType();
        double maxHealth = playerClass.getMaxHealth(upgrades);
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(maxHealth);
        if (player.getHealth() > maxHealth) player.setHealth(maxHealth);

        player.sendMessage("§aUpgrade " + upgrade.getDisplayName() + " to level " + upgrades.getLevel(upgrade) + " purchased!");

        UpgradeGUI.open(player, upgrades);
    }
}
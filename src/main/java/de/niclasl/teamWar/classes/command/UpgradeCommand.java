package de.niclasl.teamWar.classes.command;

import de.niclasl.teamWar.classes.gui.UpgradeGUI;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

public class UpgradeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        PlayerUpgrades upgrades = ClassManager.getUpgrades(player);

        if (upgrades == null || upgrades.getClassType() == null) {
            player.sendMessage("§cYou haven't chosen a class yet!");
            return true;
        }

        UpgradeGUI.open(player, upgrades);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        return Collections.emptyList();
    }
}

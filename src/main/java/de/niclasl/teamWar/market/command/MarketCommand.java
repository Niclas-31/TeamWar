package de.niclasl.teamWar.market.command;

import de.niclasl.teamWar.market.gui.MarketMainGui;
import de.niclasl.teamWar.market.manager.MarketManager;
import de.niclasl.teamWar.market.manager.MarketManager.Category;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarketCommand implements CommandExecutor, TabCompleter {

    private static MarketManager marketManager;

    public MarketCommand(MarketManager marketManager) {
        MarketCommand.marketManager = marketManager;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("market")) {
            player.sendMessage("§cYou don't have permission to use the market!");
            return true;
        }

        if (args.length == 0) {
            MarketMainGui.open(player, Category.FOOD, 0);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("market.reload")) {
                player.sendMessage("§cYou don't have permission to reload the market config!");
                return true;
            }
            marketManager.loadMarketConfig();
            player.sendMessage("§aMarket config reloaded!");
            return true;
        }

        player.sendMessage("§cUsage: /markt");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 1) {
            List<String> subs = List.of("reload");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

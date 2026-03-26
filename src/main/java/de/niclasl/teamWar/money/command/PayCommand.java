package de.niclasl.teamWar.money.command;

import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class PayCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can pay!");
            return true;
        }

        if (!sender.hasPermission("pay")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (player.equals(target)) {
            player.sendMessage(ChatColor.RED + "You cannot pay yourself!");
            return true;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1].replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
            return true;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be greater than 0!");
            return true;
        }

        String playerTeam = TeamManager.getTeamOfPlayer(player.getUniqueId());
        String targetTeam = TeamManager.getTeamOfPlayer(target.getUniqueId());

        if (playerTeam == null) {
            player.sendMessage(ChatColor.RED + "You are not in a team!");
            return true;
        }
        if (targetTeam == null) {
            player.sendMessage(ChatColor.RED + target.getName() + " is not in a team!");
            return true;
        }

        if (MoneyManager.getTeamMoney(playerTeam) < amount.doubleValue()) {
            player.sendMessage(ChatColor.RED + "Not enough money in your team!");
            return true;
        }

        MoneyManager.removeTeamMoney(playerTeam, amount.doubleValue());
        MoneyManager.addTeamMoney(targetTeam, amount.doubleValue());

        String formatted = MoneyManager.getTeamMoneyFormatted(playerTeam);

        player.sendMessage(ChatColor.GREEN + "Paid " + formatted + "$ from your team to " + target.getName() + "'s team!");
        target.sendMessage(ChatColor.GREEN + "Received " + formatted + "$ for your team from " + player.getName() + "'s team!");

        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}

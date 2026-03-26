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
import java.util.*;
import java.util.stream.Collectors;

public class MoneyCommand implements CommandExecutor, TabCompleter {

    private static MoneyManager moneyManager;

    public MoneyCommand(MoneyManager moneyManager) {
        MoneyCommand.moneyManager = moneyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("eco")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length == 0) return true;

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "help": {
                if (!sender.hasPermission("eco.help")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Economy Commands:");
                sender.sendMessage(ChatColor.YELLOW + "/money give <player> <amount>");
                sender.sendMessage(ChatColor.YELLOW + "/money take <player> <amount>");
                sender.sendMessage(ChatColor.YELLOW + "/money set <player> <amount>");
                sender.sendMessage(ChatColor.YELLOW + "/money reload");
                return true;
            }

            case "give": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can give money!");
                    return true;
                }
                if (!sender.hasPermission("eco.give")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /money give <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                if (player.equals(target)) {
                    player.sendMessage(ChatColor.RED + "You cannot give money to yourself!");
                    return true;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(args[2].replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount!");
                    return true;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    player.sendMessage(ChatColor.RED + "Amount must be greater than 0!");
                    return true;
                }

                String playerTeam = TeamManager.getTeamOfPlayer(player.getUniqueId());
                String targetTeam = TeamManager.getTeamOfPlayer(target.getUniqueId());

                boolean playerHasTeam = playerTeam != null;
                boolean targetHasTeam = targetTeam != null;

                if (playerHasTeam) {
                    if (MoneyManager.getTeamMoney(playerTeam) < amount.doubleValue()) {
                        player.sendMessage(ChatColor.RED + "Not enough money in your team!");
                        return true;
                    }
                    MoneyManager.removeTeamMoney(playerTeam, amount.doubleValue());
                } else {
                    if (MoneyManager.getPlayerMoney(player.getUniqueId()) < amount.doubleValue()) {
                        player.sendMessage(ChatColor.RED + "You don't have enough money!");
                        return true;
                    }
                    MoneyManager.removePlayerMoney(player.getUniqueId(), amount.doubleValue());
                }

                if (targetHasTeam) {
                    MoneyManager.addTeamMoney(targetTeam, amount.doubleValue());
                } else {
                    MoneyManager.addPlayerMoney(target.getUniqueId(), amount.doubleValue());
                }

                String formatted = amount.setScale(2, RoundingMode.HALF_UP).toString();
                player.sendMessage(ChatColor.GREEN + "Gave " + formatted + "$ to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You received " + formatted + "$ from " + player.getName());
                return true;
            }

            case "take": case "remove": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can take money!");
                    return true;
                }
                if (!sender.hasPermission("eco.take")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /money take <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(args[2].replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount!");
                    return true;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be greater than 0!");
                    return true;
                }

                String targetTeam = TeamManager.getTeamOfPlayer(target.getUniqueId());
                if (targetTeam != null) {
                    MoneyManager.removeTeamMoney(targetTeam, amount.doubleValue());
                } else {
                    MoneyManager.removePlayerMoney(target.getUniqueId(), amount.doubleValue());
                }

                String formatted = amount.setScale(2, RoundingMode.HALF_UP).toString();
                sender.sendMessage(ChatColor.GREEN + "Removed " + formatted + "$ from " + target.getName());
                target.sendMessage(ChatColor.RED + formatted + "$ were removed by " + ChatColor.YELLOW + sender.getName());
                return true;
            }

            case "set": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can set money!");
                    return true;
                }
                if (!sender.hasPermission("eco.set")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /money set <player> <amount>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(args[2].replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount!");
                    return true;
                }

                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    sender.sendMessage(ChatColor.RED + "Amount cannot be negative!");
                    return true;
                }

                String targetTeam = TeamManager.getTeamOfPlayer(target.getUniqueId());
                if (targetTeam != null) {
                    MoneyManager.setTeamMoney(targetTeam, amount.doubleValue());
                    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s team money to " + amount);
                    target.sendMessage(ChatColor.GREEN + "Your team's balance was set to " + amount + "$ by " + sender.getName());
                } else {
                    MoneyManager.setPlayerMoney(target.getUniqueId(), amount.doubleValue());
                    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s money to " + amount);
                    target.sendMessage(ChatColor.GREEN + "Your balance was set to " + amount + "$ by " + sender.getName());
                }
                return true;
            }

            case "reload": {
                if (!sender.hasPermission("eco.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                moneyManager.reload();
                sender.sendMessage(ChatColor.GREEN + "Economy config reloaded!");
                return true;
            }

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("help","give","take","remove","set","reload");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && Arrays.asList("give","take","remove","set").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
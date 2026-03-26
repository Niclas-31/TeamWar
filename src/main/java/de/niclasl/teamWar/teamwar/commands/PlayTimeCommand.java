package de.niclasl.teamWar.teamwar.commands;

import de.niclasl.teamWar.teamwar.manager.PlayerTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class PlayTimeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can see their own playing time!");
            return true;
        }

        if (!player.hasPermission("playtime")) {
            player.sendMessage("§cYou don't have permission to use the playtime command!");
            return true;
        }

        if (args.length == 0) {
            long seconds = PlayerTimeManager.getDailyPlayTime(player.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Your playing time today: " + formatTime(seconds));
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            long seconds = PlayerTimeManager.getDailyPlayTime(target.getUniqueId());
            sender.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " today's game time: " + formatTime(seconds));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /playtime [player]");
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

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, sec);
    }
}

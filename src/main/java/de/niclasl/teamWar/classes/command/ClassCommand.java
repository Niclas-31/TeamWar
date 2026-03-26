package de.niclasl.teamWar.classes.command;

import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.classes.PlayerClass;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassCommand implements CommandExecutor, TabCompleter {

    private static ClassManager classManager;

    public ClassCommand(ClassManager manager) {
        classManager = manager;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can choose classes!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§7Use: §e/class <healer|tank|miner|archer|builder>");
            return true;
        }

        Team team = TeamManager.getTeam(player.getUniqueId());
        if (team == null) {
            player.sendMessage("§cYou are not in a team!");
            return true;
        }

        if (team.hasClass(player.getUniqueId())) {
            player.sendMessage("§cYou already have a class and cannot switch!");
            return true;
        }

        String className = args[0].toLowerCase();
        List<String> validClasses = Arrays.asList("healer", "tank", "miner", "archer", "builder");
        if (!validClasses.contains(className)) {
            player.sendMessage("§cInvalid class! Valid classes: healer, tank, miner, archer, builder");
            return true;
        }

        if (!team.assignClass(className, player.getUniqueId())) {
            player.sendMessage("§cThis class is already taken in your team!");
            return true;
        }

        PlayerClass playerClass = PlayerClass.valueOf(className.toUpperCase());
        classManager.setClass(player, playerClass);

        TeamManager.saveTeam(team.getName());

        player.sendMessage("§aYou are now a " + className + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> allClasses = Arrays.asList("healer", "tank", "miner", "archer", "builder");
            return allClasses.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
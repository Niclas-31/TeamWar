package de.niclasl.teamWar.teamwar.commands;

import de.niclasl.playerManagementCore.portal.api.PortalApi;
import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.money.listener.BlockBreakRewardListener;
import de.niclasl.teamWar.teamwar.manager.*;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class TeamWarCommand implements CommandExecutor, TabCompleter {

    private static TeamManager manager;
    private final GameStateManager gameStateManager;
    private final BedManager bedManager;
    private final TeamWar plugin;

    public TeamWarCommand(TeamManager manager, GameStateManager gameStateManager, BedManager bedManager, TeamWar plugin) {
        TeamWarCommand.manager = manager;
        this.gameStateManager = gameStateManager;
        this.bedManager = bedManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!sender.hasPermission("teamwar")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§aTeamWar - /teamwar <create|delete|setspawn|setbed|setbase|setlobby|lobby|join|leave|start|stop|status>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "create": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can create a Team!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.create")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /teamwar create <name>");
                    return true;
                }

                String teamName = args[1].toLowerCase();
                if (TeamManager.exists(teamName)) {
                    player.sendMessage("§cTeam already exists");
                    return true;
                }

                TeamManager.createTeam(teamName);
                TeamManager.saveTeam(teamName);
                player.sendMessage("§aTeam created: §e" + teamName);
                return true;
            }

            case "delete": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can delete a Team!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.delete")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /teamwar delete <name>");
                    return true;
                }

                String teamName = args[1].toLowerCase();
                if (!TeamManager.exists(teamName)) {
                    player.sendMessage("§cTeam doesn't exist!");
                    return true;
                }

                manager.deleteTeam(teamName);
                bedManager.removeBed(teamName);
                bedManager.removeRespawnForTeam(teamName);
                manager.deleteTeamFile(teamName);
                player.sendMessage("§aTeam deleted: §e" + teamName);
                return true;
            }

            case "setspawn": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can set spawn!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.setspawn")) {
                    player.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /teamwar setspawn <team>");
                    return true;
                }

                String teamName = args[1].toLowerCase();
                if (!TeamManager.exists(teamName)) {
                    player.sendMessage("§cTeam doesn't exist!");
                    return true;
                }

                Location loc = player.getLocation();
                Team team = TeamManager.getTeamByName(teamName);
                if (team != null) team.setSpawn(loc);
                TeamManager.getTeamSpawns().put(teamName, loc);
                TeamManager.saveTeam(teamName);
                TeamManager.setTeamSpawn(teamName, loc);
                player.sendMessage("§aSpawn set for team §e" + teamName);
                return true;
            }

            case "setbed": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can set beds!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.setbed")) {
                    player.sendMessage("§cNo permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /teamwar setbed <team>");
                    return true;
                }

                String teamName = args[1].toLowerCase();
                if (!TeamManager.exists(teamName)) {
                    player.sendMessage("§cTeam doesn't exist!");
                    return true;
                }

                Location base = player.getLocation().clone();
                base.setX(base.getBlockX());
                base.setY(base.getBlockY());
                base.setZ(base.getBlockZ());

                Location head = base.clone();
                switch (player.getFacing()) {
                    case NORTH -> head.add(0, 0, -1);
                    case SOUTH -> head.add(0, 0, 1);
                    case WEST  -> head.add(-1, 0, 0);
                    case EAST  -> head.add(1, 0, 0);
                }

                bedManager.setBedForTeam(teamName, base, head);

                player.sendMessage("§a Bed set for team §e" + teamName +
                        " §aat X:" + base.getBlockX() +
                        " Y:" + base.getBlockY() +
                        " Z:" + base.getBlockZ());
                return true;
            }

            case "setbase": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can set bases!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.setbase")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                Team team = TeamManager.getTeam(player.getUniqueId());
                if (team == null) {
                    player.sendMessage("§cYou're not on any team!");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7Use: §e/teamwar setbase <pos1|pos2>");
                    return true;
                }

                if (args[1].equalsIgnoreCase("pos1")) {
                    team.setPos1(player.getLocation());
                    player.sendMessage("§a Base point 1 set at: X=" + player.getLocation().getBlockX() +
                            " Y=" + player.getLocation().getBlockY() +
                            " Z=" + player.getLocation().getBlockZ());
                } else if (args[1].equalsIgnoreCase("pos2")) {
                    team.setPos2(player.getLocation());
                    player.sendMessage("§a Base point 2 set at: X=" + player.getLocation().getBlockX() +
                            " Y=" + player.getLocation().getBlockY() +
                            " Z=" + player.getLocation().getBlockZ());
                } else {
                    player.sendMessage("§cInvalid parameter! Use pos1 or pos2.");
                    return true;
                }

                TeamManager.saveTeam(team.getName());

                if (team.getPos1() != null && team.getPos2() != null) {
                    player.sendMessage("§eTeam base has been set from: " +
                            team.getPos1().getBlockX() + " " + team.getPos1().getBlockY() + " " + team.getPos1().getBlockZ() +
                            " to: " +
                            team.getPos2().getBlockX() + " " + team.getPos2().getBlockY() + " " + team.getPos2().getBlockZ());
                }

                break;
            }

            case "setlobby": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.setlobby")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                plugin.getLobbyManager().setLobbySpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Lobby spawn set!");
                return true;
            }

            case "lobby": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }

                if (!sender.hasPermission("teamwar.lobby")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                if (LobbyManager.getLobbySpawn() == null) {
                    player.sendMessage(ChatColor.RED + "Lobby spawn is not set yet!");
                    return true;
                }

                gameStateManager.setGameState(TeamWarManager.GameState.LOBBY);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    PortalApi.markTeleport(p, "TeamWar");
                }

                plugin.getLobbyManager().teleportAllToLobby();

                sender.sendMessage(ChatColor.GREEN + "All players have been teleported to the lobby!");
                LobbyManager.addPlayerToLobby(player);
                BlockBreakRewardListener.resetBlocks();
                return true;
            }

            case "join": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can join!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.join")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                if (player.isOp()) {
                    player.sendMessage("§cAdmins cannot join a team!");
                    return true;
                }
                if (LobbyManager.isInLobby(player)) {
                    player.sendMessage("§eYou are in the game and cannot use this command!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /teamwar join <team>");
                    return true;
                }

                String teamName = args[1].toLowerCase();
                if (!TeamManager.exists(teamName)) {
                    player.sendMessage("§cTeam doesn't exist!");
                    return true;
                }
                if (manager.isInTeam(player.getUniqueId())) {
                    player.sendMessage("§cYou are already in a team!");
                    return true;
                }

                int maxPlayers = 5;
                if (TeamManager.getTeamPlayers(teamName).size() >= maxPlayers) {
                    player.sendMessage("§cThis team is full! Max " + maxPlayers + " players allowed.");
                    return true;
                }

                manager.addPlayerToTeam(player.getUniqueId(), teamName);
                TeamManager.saveTeam(teamName);
                player.sendMessage("§aYou joined team: §e" + teamName);
                return true;
            }

            case "leave": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can leave!");
                    return true;
                }
                if (!sender.hasPermission("teamwar.leave")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }
                if (LobbyManager.isInLobby(player)) {
                    player.sendMessage("§eYou are in the game and cannot use this command!");
                    return true;
                }
                if (!manager.isInTeam(player.getUniqueId())) {
                    player.sendMessage("§cYou are not in any team!");
                    return true;
                }

                Team team = manager.getTeam(player.getUniqueId().toString());
                if (team != null) {
                    String teamName = team.getName().toLowerCase();
                    manager.removePlayerFromTeam(player.getUniqueId());
                    TeamManager.saveTeam(teamName);
                } else {
                    manager.removePlayerFromTeam(player.getUniqueId());
                }

                player.sendMessage("§aYou left your team");
                return true;
            }

            case "start": {
                if (!sender.hasPermission("teamwar.start")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                if (GameStateManager.getGameState() == TeamWarManager.GameState.RUNNING) {
                    sender.sendMessage("§cThe game is already running!");
                    return true;
                }

                gameStateManager.setGameState(TeamWarManager.GameState.RUNNING);

                for (UUID uuid : TeamManager.getPlayerTeams().keySet()) {

                    Player player = Bukkit.getPlayer(uuid);

                    if (player != null && player.isOnline()) {
                        String teamName = TeamManager.getPlayerTeams().get(uuid);
                        Location spawnLocation = TeamManager.getTeamSpawns().get(teamName.toLowerCase());
                        if (spawnLocation != null) {
                            PortalApi.markTeleport(player, "TeamWar");
                            player.teleport(spawnLocation);
                        }

                        LobbyManager.removeFromLobby(player);
                        player.sendMessage("§aYou have been teleported to your team spawn.");
                    }
                }

                sender.sendMessage("§aThe game has started!");

                PvPManager.startGame();
                plugin.getBorderManager().startGameBorder();
                bedManager.spawnTeamBeds();

                return true;
            }

            case "stop": {
                if (!sender.hasPermission("teamwar.stop")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                if (GameStateManager.getGameState() == TeamWarManager.GameState.LOBBY
                        || GameStateManager.getGameState() == TeamWarManager.GameState.ENDED) {
                    sender.sendMessage("§cThe game is already stopped!");
                    return true;
                }

                if (LobbyManager.getLobbySpawn() == null) {
                    sender.sendMessage(ChatColor.RED + "Lobby spawn is not set!");
                    return true;
                }

                sender.sendMessage("§cStopping the game...");

                gameStateManager.setGameState(TeamWarManager.GameState.ENDED);

                PvPManager.resetGame();
                bedManager.removeAllBeds();
                BlockBreakRewardListener.resetBlocks();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    PortalApi.markTeleport(p, "Teamwar");
                    LobbyManager.addPlayerToLobby(p);
                }

                plugin.getLobbyManager().teleportAllToLobby();

                gameStateManager.setGameState(TeamWarManager.GameState.LOBBY);
                plugin.getBorderManager().stopGameBorder();

                Bukkit.broadcastMessage("§cThe game has been stopped!");
                return true;
            }

            case "status": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }

                if (!sender.hasPermission("teamwar.status")) {
                    sender.sendMessage("§cNo permission!");
                    return true;
                }

                sender.sendMessage("§aTeams: §6" + String.join(", ", TeamManager.getTeams().keySet()));
                sender.sendMessage("§aTeam Spawns: §6" + String.join(", ", TeamManager.getTeamSpawns().keySet()));
                return true;
            }

            default:
                sender.sendMessage("§cUnknown subcommand.");
                return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> subs = Arrays.asList("create", "delete", "join", "leave", "lobby", "setlobby", "setspawn", "setbase", "setbed", "start", "stop", "status");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("join") || sub.equals("setspawn") || sub.equals("delete") || sub.equals("setbed")) {
                return TeamManager.getTeams().keySet().stream()
                        .filter(t -> t.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setbase")) {
            List<String> options = Arrays.asList("pos1", "pos2");

            Location loc = player.getLocation();
            player.sendMessage("§7Your current position: X=" + loc.getBlockX() + " Y=" + loc.getBlockY() + " Z=" + loc.getBlockZ());

            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
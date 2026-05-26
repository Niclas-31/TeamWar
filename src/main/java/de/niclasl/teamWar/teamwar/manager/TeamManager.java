package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.classes.PlayerClass;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {

    private static TeamWar plugin;

    private static final Map<String, Location> teamSpawns = new HashMap<>();
    private static final Map<UUID, String> playerTeams = new HashMap<>();

    private static final Map<String, Team> teams = new HashMap<>();
    private static final Map<String, ChatColor> teamColors = Map.ofEntries(
            Map.entry("black", ChatColor.BLACK),
            Map.entry("dark_blue", ChatColor.DARK_BLUE),
            Map.entry("dark_green", ChatColor.DARK_GREEN),
            Map.entry("dark_aqua", ChatColor.DARK_AQUA),
            Map.entry("dark_red", ChatColor.DARK_RED),
            Map.entry("dark_purple", ChatColor.DARK_PURPLE),
            Map.entry("gold", ChatColor.GOLD),
            Map.entry("gray", ChatColor.GRAY),
            Map.entry("dark_gray", ChatColor.DARK_GRAY),
            Map.entry("blue", ChatColor.BLUE),
            Map.entry("green", ChatColor.GREEN),
            Map.entry("aqua", ChatColor.AQUA),
            Map.entry("red", ChatColor.RED),
            Map.entry("light_purple", ChatColor.LIGHT_PURPLE),
            Map.entry("yellow", ChatColor.YELLOW),
            Map.entry("white", ChatColor.WHITE)
    );

    public TeamManager(TeamWar plugin) {
        TeamManager.plugin = plugin;
    }

    public static Map<String, Location> getTeamSpawns() {
        return teamSpawns;
    }

    public static Map<UUID, String> getPlayerTeams() {
        return playerTeams;
    }

    public static Map<String, Team> getTeams() {
        return teams;
    }

    public static void loadAllTeams() {
        teams.clear();
        teamSpawns.clear();
        playerTeams.clear();

        File teamsFolder = new File(plugin.getDataFolder(), "teams");
        if (!teamsFolder.exists()) {
            teamsFolder.mkdirs();
        }

        File[] files = teamsFolder.listFiles((_, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String teamKey = file.getName().replace(".yml", "");
            loadTeams(teamKey);
        }
    }

    public static void loadTeams(String teamKey) {
        File file = new File(plugin.getDataFolder() + "/teams", teamKey.toLowerCase() + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String teamName = cfg.getString("teamName", teamKey).toLowerCase();
        Team team = new Team(plugin, teamName);

        if (cfg.isConfigurationSection("spawn")) {
            String worldName = cfg.getString("spawn.world");
            if (worldName == null) {
                plugin.getLogger().warning("Spawn world missing for team " + teamName);
            } else {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location loc = new Location(
                            world,
                            cfg.getDouble("spawn.x"),
                            cfg.getDouble("spawn.y"),
                            cfg.getDouble("spawn.z"),
                            (float) cfg.getDouble("spawn.yaw"),
                            (float) cfg.getDouble("spawn.pitch")
                    );
                    team.setSpawn(loc);
                    teamSpawns.put(teamName, loc);
                } else {
                    plugin.getLogger().warning("World '" + worldName + "' not found for team " + teamName);
                }
            }
        }

        if (cfg.isConfigurationSection("base.pos1")) {
            String worldName = cfg.getString("base.pos1.world");
            assert worldName != null;
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location pos1 = new Location(
                        world,
                        cfg.getDouble("base.pos1.x"),
                        cfg.getDouble("base.pos1.y"),
                        cfg.getDouble("base.pos1.z")
                );
                team.setPos1(pos1);
            }
        }

        if (cfg.isConfigurationSection("base.pos2")) {
            String worldName = cfg.getString("base.pos2.world");
            assert worldName != null;
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location pos2 = new Location(
                        world,
                        cfg.getDouble("base.pos2.x"),
                        cfg.getDouble("base.pos2.y"),
                        cfg.getDouble("base.pos2.z")
                );
                team.setPos2(pos2);
            }
        }

        if (cfg.isConfigurationSection("classes")) {
            for (String className : Objects.requireNonNull(cfg.getConfigurationSection("classes")).getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(Objects.requireNonNull(cfg.getString("classes." + className)));
                    team.getClassPlayers().put(className.toLowerCase(), uuid);

                    PlayerClass pc = PlayerClass.valueOf(className.toUpperCase());
                    ClassManager.setClass(uuid, pc);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        for (String uuidStr : cfg.getStringList("players")) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                playerTeams.put(uuid, teamName);
                team.addMember(uuid);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in team " + teamName + ": " + uuidStr);
            }
        }

        teams.put(teamName, team);
    }

    public static Collection<UUID> getTeamPlayers(String teamName) {
        Team team = teams.get(teamName.toLowerCase());
        if (team != null) {
            return new ArrayList<>(team.getPlayers());
        }
        return Collections.emptyList();
    }

    public static Team getTeam(UUID playerUUID) {
        String teamKey = playerTeams.get(playerUUID);
        if (teamKey == null) return null;
        return teams.get(teamKey.toLowerCase());
    }

    public static void setTeamSpawn(String teamName, Location location) {
        if (teamName == null || teamName.isEmpty() || location == null) return;

        Team team = teams.get(teamName.toLowerCase());
        if (team == null) {
            Bukkit.getLogger().warning("[TeamWar] Team " + teamName + " does not exist!");
            return;
        }

        team.setSpawn(location);

        saveTeam(teamName);

        Bukkit.broadcastMessage("§aThe spawn point for team §e" + teamName + "§a has been set!");
    }

    public Team getTeam(String playerIdString) {
        try {
            UUID playerUUID = UUID.fromString(playerIdString);
            return getTeam(playerUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Team getTeamByName(String teamName) {
        if (teamName == null) return null;
        return teams.get(teamName.toLowerCase());
    }

    public static void saveTeam(String teamKey) {
        Team team = teams.get(teamKey.toLowerCase());
        if (team == null) return;

        File file = new File(plugin.getDataFolder() + "/teams", teamKey.toLowerCase() + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("teamName", team.getName());

        Location loc = team.getSpawn();
        if (loc != null && loc.getWorld() != null) {
            cfg.set("spawn.world", loc.getWorld().getName());
            cfg.set("spawn.x", loc.getX());
            cfg.set("spawn.y", loc.getY());
            cfg.set("spawn.z", loc.getZ());
            cfg.set("spawn.yaw", loc.getYaw());
            cfg.set("spawn.pitch", loc.getPitch());
        }

        List<String> playerUUIDs = team.getMembers().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        if (!playerUUIDs.isEmpty()) {
            cfg.set("players", playerUUIDs);
        }

        Location pos1 = team.getPos1();
        if (pos1 != null && pos1.getWorld() != null) {
            cfg.set("base.pos1.world", pos1.getWorld().getName());
            cfg.set("base.pos1.x", pos1.getX());
            cfg.set("base.pos1.y", pos1.getY());
            cfg.set("base.pos1.z", pos1.getZ());
        }

        Location pos2 = team.getPos2();
        if (pos2 != null && pos2.getWorld() != null) {
            cfg.set("base.pos2.world", pos2.getWorld().getName());
            cfg.set("base.pos2.x", pos2.getX());
            cfg.set("base.pos2.y", pos2.getY());
            cfg.set("base.pos2.z", pos2.getZ());
        }

        Map<String, UUID> classPlayers = team.getClassPlayers();
        if (!classPlayers.isEmpty()) {
            for (Map.Entry<String, UUID> entry : classPlayers.entrySet()) {
                cfg.set("classes." + entry.getKey(), entry.getValue().toString());
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exists(String teamName) {
        return teams.containsKey(teamName.toLowerCase());
    }

    public void deleteTeamFile(String teamKey) {
        if (teamKey == null) return;
        File file = new File(plugin.getDataFolder(), "teams/" + teamKey.toLowerCase() + ".yml");
        if (file.exists()) {
            if (!file.delete()) {
                plugin.getLogger().warning("Could not delete team file: " + file.getName());
            } else {
                plugin.getLogger().info("Deleted team file: " + file.getName());
            }
        }
    }

    public static void createTeam(String teamName) {
        teamName = teamName.toLowerCase();

        if (teams.containsKey(teamName)) {
            return;
        }

        Team team = new Team(plugin, teamName);
        teams.put(teamName, team);
        teamSpawns.put(teamName, null);

        org.bukkit.scoreboard.Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
        }

        ChatColor color = teamColors.getOrDefault(teamName.toLowerCase(), ChatColor.WHITE);
        String prefix = color + "[" + teamName.substring(0, 1).toUpperCase() + teamName.substring(1) + "] " + ChatColor.RESET;
        scoreboardTeam.setPrefix(prefix);

        File file = new File(plugin.getDataFolder() + "/teams", teamName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                cfg.set("teamName", teamName);
                cfg.set("spawn", null);
                cfg.set("players", new ArrayList<String>());
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteTeam(String teamName) {
        String key = teamName.toLowerCase();
        teamSpawns.remove(key);
        teams.remove(key);
        playerTeams.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(key));
        deleteTeamFile(key);
    }

    public static void setBedAlive(String teamName, boolean alive) {
        Team team = teams.get(teamName.toLowerCase());
        if (team != null) {
            team.setBedAlive(alive);
        }
    }

    public void addPlayerToTeam(UUID playerUUID, String teamName) {
        Player player = Bukkit.getPlayer(playerUUID);
        teamName = teamName.toLowerCase();

        if (!teams.containsKey(teamName)) return;

        for (Team team : teams.values()) {
            team.removeMember(playerUUID);
        }

        playerTeams.put(playerUUID, teamName);
        teams.get(teamName).addMember(playerUUID);
        saveTeam(teamName);

        if (player != null) {
            org.bukkit.scoreboard.Scoreboard sb = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

            for (org.bukkit.scoreboard.Team sbTeam : sb.getTeams()) {
                if (sbTeam.hasEntry(player.getName())) sbTeam.removeEntry(player.getName());
            }

            org.bukkit.scoreboard.Team sbTeam = sb.getTeam(teamName);
            if (sbTeam == null) {
                sbTeam = sb.registerNewTeam(teamName);

                ChatColor color = ChatColor.RED;
                sbTeam.setPrefix(color + "[" + teamName.substring(0, 1).toUpperCase() + teamName.substring(1) + "] " + ChatColor.RESET);
            }

            sbTeam.addEntry(player.getName());
        }

        if (player != null) {
            String prefix = getTeamPrefix(teamName);
            player.setPlayerListName(prefix + player.getName());
            player.setDisplayName(prefix + player.getName());
        }
    }

    public void removePlayerFromTeam(UUID playerUUID) {
        String teamName = playerTeams.remove(playerUUID);
        if (teamName != null) {
            Team team = teams.get(teamName.toLowerCase());
            if (team != null) {
                team.removeMember(playerUUID);

                File file = new File(plugin.getDataFolder() + "/teams", teamName.toLowerCase() + ".yml");
                if (file.exists()) {
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                    List<String> players = cfg.getStringList("players");
                    players.remove(playerUUID.toString());
                    cfg.set("players", players);

                    try {
                        cfg.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            org.bukkit.scoreboard.Scoreboard sb = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
            for (org.bukkit.scoreboard.Team sbTeam : sb.getTeams()) {
                if (sbTeam.hasEntry(player.getName())) {
                    sbTeam.removeEntry(player.getName());
                }
            }
        }

        if (player != null) {
            player.setPlayerListName(player.getName());
            player.setDisplayName(player.getName());
        }
    }

    public boolean isInTeam(UUID playerUUID) {
        return playerTeams.containsKey(playerUUID);
    }

    public static String getTeamOfPlayer(UUID uuid) {
        return playerTeams.get(uuid);
    }

    public static Team[] getAllTeams() {
        return teams.values().toArray(new Team[0]);
    }

    public static String getTeamPrefix(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "black" -> ChatColor.BLACK + "[Black] " + ChatColor.RESET;
            case "dark_blue" -> ChatColor.DARK_BLUE + "[Dark Blue] " + ChatColor.RESET;
            case "dark_green" -> ChatColor.DARK_GREEN + "[Dark Green] " + ChatColor.RESET;
            case "dark_aqua" -> ChatColor.DARK_AQUA + "[Dark Aqua] " + ChatColor.RESET;
            case "dark_red" -> ChatColor.DARK_RED + "[Dark Red] " + ChatColor.RESET;
            case "dark_purple" -> ChatColor.DARK_PURPLE + "[Dark Purple] " + ChatColor.RESET;
            case "gold" -> ChatColor.GOLD + "[Gold] " + ChatColor.RESET;
            case "gray" -> ChatColor.GRAY + "[Gray] " + ChatColor.RESET;
            case "dark_gray" -> ChatColor.DARK_GRAY + "[Dark Gray] " + ChatColor.RESET;
            case "blue" -> ChatColor.BLUE + "[Blue] " + ChatColor.RESET;
            case "green" -> ChatColor.GREEN + "[Green] " + ChatColor.RESET;
            case "aqua" -> ChatColor.AQUA + "[Aqua] " + ChatColor.RESET;
            case "red" -> ChatColor.RED + "[Red] " + ChatColor.RESET;
            case "light_purple" -> ChatColor.LIGHT_PURPLE + "[Light Purple] " + ChatColor.RESET;
            case "yellow" -> ChatColor.YELLOW + "[Yellow] " + ChatColor.RESET;
            case "white" -> ChatColor.WHITE + "[White] " + ChatColor.RESET;
            default -> "";
        };
    }
}
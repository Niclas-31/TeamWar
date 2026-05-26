package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BedManager {

    private final Map<String, List<Location>> teamBeds = new HashMap<>();
    private final Map<String, Float> teamBedYaw = new HashMap<>();
    private final TeamWar plugin;

    public BedManager(TeamWar plugin) {
        this.plugin = plugin;
    }

    public void setBedForTeam(String teamName, Location foot, Location head) {
        float yaw = foot.getYaw();
        teamBeds.put(teamName.toLowerCase(), Arrays.asList(foot, head));
        teamBedYaw.put(teamName.toLowerCase(), yaw);
        TeamManager.setBedAlive(teamName, true);

        File file = new File(plugin.getDataFolder() + "/teams", teamName.toLowerCase() + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("bed.world", Objects.requireNonNull(foot.getWorld()).getName());

        cfg.set("bed.foot.x", foot.getBlockX());
        cfg.set("bed.foot.y", foot.getBlockY());
        cfg.set("bed.foot.z", foot.getBlockZ());

        cfg.set("bed.head.x", head.getBlockX());
        cfg.set("bed.head.y", head.getBlockY());
        cfg.set("bed.head.z", head.getBlockZ());

        cfg.set("bed.yaw", yaw);

        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAllBeds() {
        File teamsFolder = new File(plugin.getDataFolder(), "teams");
        if (!teamsFolder.exists()) return;

        for (File file : Objects.requireNonNull(teamsFolder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String teamKey = file.getName().replace(".yml", "");
                loadBeds(teamKey);
            }
        }
    }

    private void loadBeds(String teamKey) {
        File file = new File(plugin.getDataFolder() + "/teams", teamKey.toLowerCase() + ".yml");
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        if (!cfg.isConfigurationSection("bed")) return;

        String worldName = cfg.getString("bed.world");
        if (worldName == null) {
            Bukkit.getLogger().warning("[TeamWar] Bed for team " + teamKey + " has no world defined!");
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("[TeamWar] World '" + worldName + "' for team " + teamKey + " is not loaded!");
            return;
        }

        Location foot = new Location(
                world,
                cfg.getInt("bed.foot.x"),
                cfg.getInt("bed.foot.y"),
                cfg.getInt("bed.foot.z")
        );
        Location head = new Location(
                world,
                cfg.getInt("bed.head.x"),
                cfg.getInt("bed.head.y"),
                cfg.getInt("bed.head.z")
        );

        float yaw = (float) cfg.getDouble("bed.yaw", 0.0);

        teamBeds.put(teamKey.toLowerCase(), Arrays.asList(foot, head));
        teamBedYaw.put(teamKey.toLowerCase(), yaw);
    }

    public void removeBed(String teamName) {
        List<Location> parts = teamBeds.remove(teamName.toLowerCase());
        if (parts != null) {
            for (Location loc : parts) {
                if (loc.getBlock().getType().toString().endsWith("_BED")) {
                    loc.getBlock().setType(Material.AIR, false);
                }
            }
        }
        TeamManager.setBedAlive(teamName, false);
    }

    public void removeRespawnForTeam(String teamName) {
        for (UUID uuid : TeamManager.getPlayerTeams().keySet()) {
            if (TeamManager.getPlayerTeams().get(uuid).equalsIgnoreCase(teamName)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.setRespawnLocation(null);
                }
            }
        }
    }

    private BlockFace getFacingFromYaw(float yaw) {
        yaw = ((yaw + 180) % 360 + 360) % 360 - 180;

        if (yaw >= -135 && yaw < -45) return BlockFace.EAST;
        if (yaw >= -45 && yaw < 45) return BlockFace.SOUTH;
        if (yaw >= 45 && yaw < 135) return BlockFace.WEST;
        return BlockFace.NORTH;
    }

    public void spawnTeamBeds() {
        for (Team team : TeamManager.getTeams().values()) {
            List<Location> parts = teamBeds.get(team.getName().toLowerCase());
            if (parts != null && parts.size() == 2) {
                Location footLoc = parts.get(0);
                Location headLoc = parts.get(1);

                Material bedMaterial = getBedMaterialForTeam(team.getName());

                Block footBlock = footLoc.getBlock();
                Block headBlock = headLoc.getBlock();

                footBlock.setType(bedMaterial, false);

                headBlock.setType(bedMaterial, false);

                float yaw = teamBedYaw.getOrDefault(team.getName().toLowerCase(), footLoc.getYaw());
                BlockFace facing = getFacingFromYaw(yaw);

                if (footBlock.getBlockData() instanceof Bed footData) {
                    footData.setPart(Bed.Part.FOOT);
                    footData.setFacing(facing);
                    footBlock.setBlockData(footData, false);
                }

                if (headBlock.getBlockData() instanceof Bed headData) {
                    headData.setPart(Bed.Part.HEAD);
                    headData.setFacing(facing);
                    headBlock.setBlockData(headData, false);
                }

                team.setBedAlive(true);
            }
        }
    }

    private Material getBedMaterialForTeam(String teamName) {
        String colorName = teamName.toUpperCase();
        try {
            return Material.valueOf(colorName + "_BED");
        } catch (IllegalArgumentException e) {
            return Material.WHITE_BED;
        }
    }

    public void removeAllBeds() {
        for (List<Location> parts : teamBeds.values()) {
            for (Location loc : parts) {
                if (loc.getBlock().getType().toString().endsWith("_BED")) {
                    loc.getBlock().setType(Material.AIR, false);
                }
            }
        }
    }

    public Map<String, List<Location>> getTeamBeds() {
        return teamBeds;
    }
}
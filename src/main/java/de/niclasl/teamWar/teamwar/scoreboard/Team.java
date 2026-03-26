package de.niclasl.teamWar.teamwar.scoreboard;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Team extends TeamManager {
    private final String name;
    private Location spawn;
    private final Set<UUID> members = new HashSet<>();
    private boolean bedAlive = false;
    private final Set<UUID> players = new HashSet<>();
    private Location pos1;
    private Location pos2;

    private final Map<String, UUID> classPlayers = new HashMap<>();

    public Team(TeamWar plugin, String name) {
        super(plugin);
        this.name = name;
    }

    public static Player getPlayer(UUID playerId) {
        return Bukkit.getPlayer(playerId);
    }

    public void setPos1(Location loc) { this.pos1 = loc; }
    public Location getPos1() { return pos1; }

    public void setPos2(Location loc) { this.pos2 = loc; }
    public Location getPos2() { return pos2; }

    public boolean isInBase(Location loc) {
        if (pos1 == null || pos2 == null) return false;
        if (!Objects.requireNonNull(loc.getWorld()).getName().equals(Objects.requireNonNull(pos1.getWorld()).getName())) return false;

        double x1 = Math.min(pos1.getX(), pos2.getX());
        double y1 = Math.min(pos1.getY(), pos2.getY());
        double z1 = Math.min(pos1.getZ(), pos2.getZ());

        double x2 = Math.max(pos1.getX(), pos2.getX());
        double y2 = Math.max(pos1.getY(), pos2.getY());
        double z2 = Math.max(pos1.getZ(), pos2.getZ());

        double px = loc.getX();
        double py = loc.getY();
        double pz = loc.getZ();

        return px >= x1 && px <= x2
                && py >= y1 && py <= y2
                && pz >= z1 && pz <= z2;
    }

    public String getName() {
        return name;
    }
    public Location getSpawn() {
        return spawn;
    }
    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public Set<UUID> getMembers() {
        return members;
    }
    public void addMember(UUID playerUUID) {
        members.add(playerUUID);
    }
    public void removeMember(UUID playerUUID) {
        members.remove(playerUUID);
    }

    public boolean hasBed() {
        return bedAlive;
    }

    public void destroyBed() {
        bedAlive = false;
    }

    public void setBedAlive(boolean alive) {
        this.bedAlive = alive;
    }

    public boolean isBedAlive() {
        return bedAlive;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public Map<String, UUID> getClassPlayers() {
        return classPlayers;
    }

    public boolean assignClass(String className, UUID playerUUID) {
        className = className.toLowerCase();
        if (classPlayers.containsKey(className)) return false;
        classPlayers.put(className, playerUUID);
        return true;
    }

    public boolean hasClass(UUID playerUUID) {
        return classPlayers.containsValue(playerUUID);
    }

    public String getPlayerClass(UUID playerUUID) {
        for (Map.Entry<String, UUID> entry : classPlayers.entrySet()) {
            if (entry.getValue().equals(playerUUID)) return entry.getKey();
        }
        return null;
    }
}

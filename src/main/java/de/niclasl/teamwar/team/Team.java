package de.niclasl.teamwar.team;

import net.minecraft.core.BlockPos;

import java.util.*;

public class Team {

    private final String id;
    private final String name;

    private TeamLocation spawn;

    private BlockPos pos1;
    private BlockPos pos2;

    private boolean bedAlive;

    private final Set<UUID> members = new HashSet<>();
    private final Map<String, UUID> classPlayers = new HashMap<>();

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
        this.bedAlive = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TeamLocation getSpawn() {
        return spawn;
    }

    public void setSpawn(TeamLocation spawn) {
        this.spawn = spawn;
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    public boolean isBedAlive() {
        return bedAlive;
    }

    public void setBedAlive(boolean bedAlive) {
        this.bedAlive = bedAlive;
    }

    public void destroyBed() {
        this.bedAlive = false;
    }

    public boolean isInBase(BlockPos pos) {
        if (pos1 == null || pos2 == null) {
            return false;
        }

        int x1 = Math.min(pos1.getX(), pos2.getX());
        int y1 = Math.min(pos1.getY(), pos2.getY());
        int z1 = Math.min(pos1.getZ(), pos2.getZ());

        int x2 = Math.max(pos1.getX(), pos2.getX());
        int y2 = Math.max(pos1.getY(), pos2.getY());
        int z2 = Math.max(pos1.getZ(), pos2.getZ());

        return pos.getX() >= x1 && pos.getX() <= x2
                && pos.getY() >= y1 && pos.getY() <= y2
                && pos.getZ() >= z1 && pos.getZ() <= z2;
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

    public Map<String, UUID> getClassPlayers() {
        return classPlayers;
    }

    public boolean assignClass(String className, UUID playerUUID) {
        className = className.toLowerCase();

        if (classPlayers.containsKey(className)) {
            return false;
        }

        classPlayers.put(className, playerUUID);
        return true;
    }

    public boolean hasClass(UUID playerUUID) {
        return classPlayers.containsValue(playerUUID);
    }

    public String getPlayerClass(UUID playerUUID) {
        for (Map.Entry<String, UUID> entry : classPlayers.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
package de.niclasl.teamWar.teamwar.manager;

import de.niclasl.teamWar.TeamWar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public record LobbyManager(TeamWar plugin) {

    private static Location lobbySpawn;

    private static final Set<UUID> lobbyPlayers = new HashSet<>();

    public void setLobbySpawn(Location spawn) {
        lobbySpawn = spawn;
        plugin.getConfig().set("lobby.spawn.world", Objects.requireNonNull(spawn.getWorld()).getName());
        plugin.getConfig().set("lobby.spawn.x", spawn.getX());
        plugin.getConfig().set("lobby.spawn.y", spawn.getY());
        plugin.getConfig().set("lobby.spawn.z", spawn.getZ());
        plugin.getConfig().set("lobby.spawn.yaw", spawn.getYaw());
        plugin.getConfig().set("lobby.spawn.pitch", spawn.getPitch());
        plugin.saveConfig();
        plugin.getLogger().info("Lobby spawn saved: " + spawn);
    }

    public void loadLobbySpawn() {
        if (!plugin.getConfig().contains("lobby.spawn.world")) return;

        World world = Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("lobby.spawn.world")));
        if (world == null) return;

        double x = plugin.getConfig().getDouble("lobby.spawn.x");
        double y = plugin.getConfig().getDouble("lobby.spawn.y");
        double z = plugin.getConfig().getDouble("lobby.spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("lobby.spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("lobby.spawn.pitch");

        lobbySpawn = new Location(world, x, y, z, yaw, pitch);
    }

    public static Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void teleportToLobby(Player player) {
        player.teleport(getLobbySpawn());
        lobbyPlayers.add(player.getUniqueId());
        saveLobbyPlayers();
    }

    public void teleportAllToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportToLobby(player);
        }
    }

    public static void addPlayerToLobby(Player player) {
        lobbyPlayers.add(player.getUniqueId());
    }

    public static void removeFromLobby(Player player) {
        lobbyPlayers.remove(player.getUniqueId());
    }

    public static boolean isInLobby(Player player) {
        return !lobbyPlayers.contains(player.getUniqueId());
    }

    public void saveLobbyPlayers() {
        List<String> uuids = lobbyPlayers.stream()
                .map(UUID::toString)
                .toList();
        plugin.getConfig().set("lobby.players", uuids);
        plugin.saveConfig();
    }

    public void loadLobbyPlayers() {
        lobbyPlayers.clear();
        if (plugin.getConfig().contains("lobby.players")) {
            for (String uuidStr : plugin.getConfig().getStringList("lobby.players")) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    lobbyPlayers.add(uuid);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}

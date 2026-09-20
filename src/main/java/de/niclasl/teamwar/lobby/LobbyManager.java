package de.niclasl.teamwar.lobby;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class LobbyManager {
    private final MinecraftServer server;
    private final LobbySavedData data;

    public LobbyManager(MinecraftServer server) {
        this.server = server;
        this.data = LobbySavedData.get(server.overworld());
    }

    public void setLobbySpawn(ServerLevel level, BlockPos pos, float yaw, float pitch) {
        data.dimension = level.dimension();
        data.spawn = pos;
        data.yaw = yaw;
        data.pitch = pitch;

        data.setDirty();
    }

    public BlockPos getLobbySpawn() {
        return data.spawn;
    }

    public void teleportToLobby(ServerPlayer player) {
        if (data.spawn == null) {
            return;
        }

        ServerLevel level = server.getLevel(data.dimension);

        if (level == null) {
            return;
        }

        player.teleportTo(
                level,
                data.spawn.getX(),
                data.spawn.getY(),
                data.spawn.getZ(),
                Set.of(),
                data.pitch,
                data.yaw,
                false
        );

        data.players.add(player.getUUID());
        data.setDirty();
    }

    public void teleportAllToLobby() {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            teleportToLobby(player);
        }
    }

    public void addPlayerToLobby(Player player) {
        data.players.add(player.getUUID());
    }

    public void removePlayerFromLobby(Player player) {
        data.players.remove(player.getUUID());
    }

    public boolean isInLobby(Player player) {
        return !data.players.contains(player.getUUID());
    }
}
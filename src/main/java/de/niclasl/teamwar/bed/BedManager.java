package de.niclasl.teamwar.bed;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BedManager {

    private final MinecraftServer server;
    private final TeamManager teamManager;
    private final BedSavedData data;

    public BedManager(MinecraftServer server) {
        this.server = server;
        this.teamManager = TeamWar.getTeamManager();
        this.data = server.getDataStorage().computeIfAbsent(BedSavedData.TYPE);
    }

    public void setBedForTeam(
            String teamName,
            ServerLevel level,
            BlockPos foot, BlockPos head,
            float yHeadRot) {
        String teamKey = teamName.toLowerCase(Locale.ROOT);

        String world = level.dimension().identifier().toString();

        TeamBed bed = new TeamBed(
                world,
                foot,
                head,
                yHeadRot
        );

        data.teamBeds.put(teamKey, bed);
        data.setDirty();

        teamManager.setBedAlive(teamName, true);
    }

    public void removeBed(String teamName) {
        if (teamName == null) {
            return;
        }

        String teamKey = teamName.toLowerCase(Locale.ROOT);

        TeamBed bed = data.teamBeds.remove(teamKey);

        if (bed != null) {
            ServerLevel level = getLevel(bed.world);

            if (level != null) {
                removeBedBlock(level, bed.foot);
                removeBedBlock(level, bed.head);
            }
        }

        teamManager.setBedAlive(teamName, false);

        data.setDirty();
    }

    private void removeBedBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BedBlock) {
            level.setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL
            );
        }
    }

    public void removeRespawnForTeam(String teamName) {
        if (teamName == null) {
            return;
        }

        for (UUID uuid : teamManager.getPlayerTeams().keySet()) {

            String playerTeam = teamManager.getPlayerTeams().get(uuid);

            if (playerTeam == null ||
                    !playerTeam.equalsIgnoreCase(teamName)) {
                continue;
            }

            ServerPlayer player =
                    server.getPlayerList().getPlayer(uuid);

            if (player != null) {
                player.setRespawnPosition(null, false);
            }
        }
    }

    public void spawnTeamBeds() {
        for (Team team : teamManager.getTeams().values()) {

            String teamKey =
                    team.getName().toLowerCase(Locale.ROOT);

            TeamBed bed = data.teamBeds.get(teamKey);

            if (bed == null) {
                continue;
            }

            ServerLevel level = getLevel(bed.world);

            if (level == null) {
                continue;
            }

            BlockState footState = getBedState(team.getName(), BedPart.FOOT, bed.yaw);
            BlockState headState = getBedState(team.getName(), BedPart.HEAD, bed.yaw);

            level.setBlock(
                    bed.foot,
                    footState,
                    Block.UPDATE_ALL
            );

            level.setBlock(
                    bed.head,
                    headState,
                    Block.UPDATE_ALL
            );

            team.setBedAlive(true);
        }
    }

    private BlockState getBedState(String teamName, BedPart part, float yaw) {
        DyeColor color = getTeamBedColor(teamName);

        Direction facing = getFacingFromYaw(yaw);

        return Blocks.BED.pick(color)
                .defaultBlockState()
                .setValue(BedBlock.PART, part)
                .setValue(BedBlock.FACING, facing);
    }

    private DyeColor getTeamBedColor(String teamName) {
        try {
            return DyeColor.byName(
                    teamName.toLowerCase(Locale.ROOT),
                    DyeColor.WHITE
            );
        } catch (Exception ignored) {
            return DyeColor.WHITE;
        }
    }

    public Direction getFacingFromYaw(float yaw) {
        yaw = ((yaw + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F;

        if (yaw >= -135.0F && yaw < -45.0F) {
            return Direction.EAST;
        }

        if (yaw >= -45.0F && yaw < 45.0F) {
            return Direction.SOUTH;
        }

        if (yaw >= 45.0F && yaw < 135.0F) {
            return Direction.WEST;
        }

        return Direction.NORTH;
    }

    public void removeAllBeds() {
        for (TeamBed bed : data.teamBeds.values()) {

            ServerLevel level = getLevel(bed.world);

            if (level == null) {
                continue;
            }

            removeBedBlock(level, bed.foot);
            removeBedBlock(level, bed.head);
        }

        for (Team team : teamManager.getTeams().values()) {
            team.setBedAlive(false);
        }
    }

    private ServerLevel getLevel(String world) {
        if (world == null || world.isEmpty()) {
            return null;
        }

        Identifier identifier = Identifier.parse(world);

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, identifier);

        return server.getLevel(dimension);
    }

    public Map<String, TeamBed> getTeamBeds() {
        return data.teamBeds;
    }
}
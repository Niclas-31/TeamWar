package de.niclasl.teamwar.team;

import com.mojang.serialization.Codec;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.PlayerClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamSavedData extends SavedData {

    public static final Codec<TeamSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag ->{
                TeamSavedData instance = new TeamSavedData();
                instance.read(tag);
                return instance;
            }, instance -> instance.write(new CompoundTag())
    );
    public static final SavedDataType<TeamSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "teams"),
                    TeamSavedData::new, CODEC);

    public static TeamSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public final Map<String, Team> teams = new HashMap<>();

    public final Map<UUID, String> playerTeams = new HashMap<>();

    public final Map<String, TeamLocation> teamSpawns = new HashMap<>();

    public final Map<UUID, PlayerClass> playerClasses = new HashMap<>();

    public void read(CompoundTag tag) {
        teams.clear();

        CompoundTag teamsTag = tag.getCompoundOrEmpty("teams");

        for (String teamId : teamsTag.keySet()) {

            CompoundTag teamTag = teamsTag.getCompoundOrEmpty(teamId);

            String teamName = teamTag.getStringOr("teamName", teamId);

            Team team = new Team(teamId, teamName);

            if (teamTag.contains("spawn")) {
                CompoundTag spawnTag = teamTag.getCompoundOrEmpty("spawn");

                TeamLocation spawn = new TeamLocation(
                        spawnTag.getStringOr("world", ""),
                        spawnTag.getDoubleOr("x", 0),
                        spawnTag.getDoubleOr("y", 0),
                        spawnTag.getDoubleOr("z", 0),
                        spawnTag.getFloatOr("yaw", 0),
                        spawnTag.getFloatOr("pitch", 0)
                );

                team.setSpawn(spawn);
                teamSpawns.put(teamId, spawn);
            }
            
            if (teamTag.contains("players")) {

                ListTag players = teamTag.getListOrEmpty("players");

                for (int i = 0; i < players.size(); i++) {
                    try {
                        UUID uuid = UUID.fromString(players.getStringOr(i, ""));
                        playerTeams.put(uuid, teamId);
                        team.addMember(uuid);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            if (teamTag.contains("base.pos1")) {

                CompoundTag pos1Tag = teamTag.getCompoundOrEmpty("base.pos1");

                team.setPos1(readBlockPos(pos1Tag));
            }

            if (teamTag.contains("base.pos2")) {

                CompoundTag pos2Tag =
                        teamTag.getCompoundOrEmpty("base.pos2");

                team.setPos2(readBlockPos(pos2Tag));
            }

            if (teamTag.contains("classes")) {

                CompoundTag classesTag = teamTag.getCompoundOrEmpty("classes");

                for (String className : classesTag.keySet()) {

                    String uuidString = classesTag.getStringOr(className, "");

                    try {
                        UUID uuid = UUID.fromString(uuidString);

                        team.getClassPlayers().put(className.toLowerCase(), uuid);

                        PlayerClass pc = PlayerClass.valueOf(className.toUpperCase());

                        playerClasses.put(uuid, pc);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            teams.put(teamId, team);
        }
    }

    public CompoundTag write(CompoundTag tag) {
        CompoundTag teamsTag = new CompoundTag();

        for (Team team : teams.values()) {

            CompoundTag teamTag = new CompoundTag();

            teamTag.putString("teamName", team.getName());

            TeamLocation spawn = team.getSpawn();

            if (spawn != null) {

                CompoundTag spawnTag = createSpawnTag(spawn);

                teamTag.put("spawn", spawnTag);
            }

            if (!team.getMembers().isEmpty()) {

                ListTag players = new ListTag();

                for (UUID uuid : team.getMembers()) {

                    players.add(StringTag.valueOf(uuid.toString()));
                }

                teamTag.put("players", players);
            }

            if (team.getPos1() != null) {

                teamTag.put("base.pos1", writeBlockPos(team.getPos1()));
            }

            if (team.getPos2() != null) {

                teamTag.put("base.pos2", writeBlockPos(team.getPos2()));
            }

            if (!team.getClassPlayers().isEmpty()) {

                CompoundTag classesTag = new CompoundTag();

                for (Map.Entry<String, UUID> entry : team.getClassPlayers().entrySet()) {

                    classesTag.putString(entry.getKey(), entry.getValue().toString());
                }

                teamTag.put("classes", classesTag);
            }

            teamsTag.put(team.getId(), teamTag);
        }

        tag.put("teams", teamsTag);

        return tag;
    }

    private static @NonNull CompoundTag createSpawnTag(TeamLocation spawn) {
        CompoundTag spawnTag = new CompoundTag();

        spawnTag.putString(
                "world",
                spawn.world
        );

        spawnTag.putDouble(
                "x",
                spawn.x
        );

        spawnTag.putDouble(
                "y",
                spawn.y
        );

        spawnTag.putDouble(
                "z",
                spawn.z
        );

        spawnTag.putFloat(
                "yaw",
                spawn.yaw
        );

        spawnTag.putFloat(
                "pitch",
                spawn.pitch
        );
        return spawnTag;
    }

    private static CompoundTag writeBlockPos(BlockPos pos) {

        CompoundTag tag =
                new CompoundTag();

        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());

        return tag;
    }

    private static BlockPos readBlockPos(CompoundTag tag) {

        return new BlockPos(
                tag.getIntOr("x", 0),
                tag.getIntOr("y", 0),
                tag.getIntOr("z", 0)
        );
    }
}
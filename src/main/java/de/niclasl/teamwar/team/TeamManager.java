package de.niclasl.teamwar.team;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.classes.PlayerClass;
import de.niclasl.teamwar.classes.PlayerUpgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.*;

public class TeamManager {

    private final MinecraftServer server;
    private final TeamSavedData data;

    public TeamManager(MinecraftServer server) {
        this.server = server;
        this.data = TeamSavedData.get(server.overworld());
    }

    public Map<String, TeamLocation> getTeamSpawns() {
        return data.teamSpawns;
    }

    public Map<UUID, String> getPlayerTeams() {
        return data.playerTeams;
    }

    public Map<String, Team> getTeams() {
        return data.teams;
    }

    public Collection<UUID> getTeamPlayers(String teamName) {
        Team team = data.teams.get(teamName.toLowerCase());
        if (team != null) {
            return new ArrayList<>(team.getMembers());
        }
        return Collections.emptyList();
    }

    public Team getTeam(UUID playerUUID) {
        if (playerUUID == null) return null;
        String teamKey = data.playerTeams.get(playerUUID);
        if (teamKey == null) return null;
        return data.teams.get(teamKey.toLowerCase());
    }

    public void setTeamSpawn(String teamName, TeamLocation location) {
        if (teamName == null || teamName.isEmpty() || location == null) return;

        Team team = data.teams.get(teamName.toLowerCase());
        if (team == null) {
            TeamWar.LOGGER.warn("[TeamWar] Team {} does not exist!", teamName);
            return;
        }

        team.setSpawn(location);
        data.teamSpawns.put(teamName, location);

        data.setDirty();

        server.getPlayerList().broadcastSystemMessage(
                Component.literal(
                        "The spawn point for team "
                                + teamName
                                + " has been set!"
                ).withStyle(ChatFormatting.GREEN), false
        );
    }

    public void setPlayerClass(ServerPlayer player, PlayerClass playerClass) {
        UUID id = player.getUUID();
        ClassManager classManager = TeamWar.getClassManager();

        data.playerClasses.put(id, playerClass);

        PlayerUpgrades upgrades = classManager.getUpgrades(player.getUUID(), playerClass);

        float maxHealth = playerClass.getMaxHealth(upgrades);

        var attribute = player.getAttribute(Attributes.MAX_HEALTH);

        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
        }

        player.setHealth(maxHealth);
        data.setDirty();
    }

    public PlayerClass getPlayerClass(UUID uuid) {
        return data.playerClasses.get(uuid);
    }

    public Team getTeam(String playerIdString) {
        try {
            UUID playerUUID = UUID.fromString(playerIdString);
            return getTeam(playerUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Team getTeamByName(String teamName) {
        if (teamName == null) return null;
        return data.teams.get(teamName.toLowerCase());
    }

    public boolean exists(String teamName) {
        return data.teams.containsKey(teamName.toLowerCase());
    }

    public void createTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return;
        }

        String key = teamName.toLowerCase(Locale.ROOT);

        if (data.teams.containsKey(key)) {
            return;
        }

        Team team = new Team(key, teamName);
        data.teams.put(key, team);
        data.teamSpawns.put(key, null);

        createMinecraftTeam(team);

        data.setDirty();
    }

    private void createMinecraftTeam(Team team) {
        Scoreboard scoreboard = server.getScoreboard();

        PlayerTeam scoreboardTeam = scoreboard.getPlayerTeam(team.getId());

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.addPlayerTeam(team.getId());
        }

        scoreboardTeam.setPlayerPrefix(
                Component.literal(
                        "[" + team.getName() + "] "
                ).withStyle(ChatFormatting.WHITE)
        );
    }

    public void deleteTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return;
        }

        String key = teamName.toLowerCase(Locale.ROOT);

        data.teamSpawns.remove(key);
        data.teams.remove(key);

        data.playerTeams
                .entrySet()
                .removeIf(entry -> entry.getValue().equalsIgnoreCase(key));

        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam minecraftTeam = scoreboard.getPlayerTeam(key);

        if (minecraftTeam != null) {
            scoreboard.removePlayerTeam(minecraftTeam);
        }

        data.setDirty();
    }

    public void setBedAlive(String teamName, boolean alive) {
        Team team = data.teams.get(teamName.toLowerCase());
        if (team != null) {
            team.setBedAlive(alive);
        }
    }

    public void addPlayerToTeam(UUID playerUUID, String teamName) {
        if (playerUUID == null || teamName == null || teamName.isBlank()) {
            return;
        }

        String key = teamName.toLowerCase(Locale.ROOT);

        Team team = data.teams.get(key);

        if (team == null) {
            return;
        }

        String oldTeam = data.playerTeams.get(playerUUID);

        if (oldTeam != null) {
            Team oldTeamObject = data.teams.get(oldTeam);

            if (oldTeamObject != null) {
                oldTeamObject.removeMember(playerUUID);
            }
        }

        data.playerTeams.put(playerUUID, key);
        team.addMember(playerUUID);

        Scoreboard scoreboard = server.getScoreboard();

        PlayerTeam minecraftTeam = scoreboard.getPlayerTeam(key);

        if (minecraftTeam != null) {
            for (PlayerTeam otherTeam : scoreboard.getPlayerTeams()) {
                otherTeam.getPlayers().remove(playerUUID.toString());
            }

            minecraftTeam.getPlayers().add(playerUUID.toString());
            minecraftTeam.setPlayerPrefix(Component.literal(getTeamPrefix(key)));
        }

        data.setDirty();
    }

    public void removePlayerFromTeam(UUID playerUUID) {
        if (playerUUID == null) {
            return;
        }

        String teamName = data.playerTeams.remove(playerUUID);

        if (teamName != null) {
            Team team = data.teams.get(teamName);

            if (team != null) {
                team.removeMember(playerUUID);
            }
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

        if (player != null) {
            Scoreboard scoreboard = server.getScoreboard();

            PlayerTeam minecraftTeam = scoreboard.getPlayersTeam(player.getScoreboardName());

            if (minecraftTeam != null) {
                scoreboard.removePlayerFromTeam(player.getScoreboardName(), minecraftTeam);
            }
        }

        data.setDirty();
    }

    public void setPos1(Team team, BlockPos pos) {
        team.setPos1(pos);
        data.setDirty();
    }

    public void setPos2(Team team, BlockPos pos) {
        team.setPos2(pos);
        data.setDirty();
    }

    public boolean isInTeam(UUID playerUUID) {
        return data.playerTeams.containsKey(playerUUID);
    }

    public String getTeamOfPlayer(UUID uuid) {
        return data.playerTeams.get(uuid);
    }

    public Team[] getAllTeams() {
        return data.teams.values().toArray(new Team[0]);
    }

    public static String getTeamPrefix(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK + "[Black] " + ChatFormatting.RESET;
            case "dark_blue" -> ChatFormatting.DARK_BLUE + "[Dark Blue] " + ChatFormatting.RESET;
            case "dark_green" -> ChatFormatting.DARK_GREEN + "[Dark Green] " + ChatFormatting.RESET;
            case "dark_aqua" -> ChatFormatting.DARK_AQUA + "[Dark Aqua] " + ChatFormatting.RESET;
            case "dark_red" -> ChatFormatting.DARK_RED + "[Dark Red] " + ChatFormatting.RESET;
            case "dark_purple" -> ChatFormatting.DARK_PURPLE + "[Dark Purple] " + ChatFormatting.RESET;
            case "gold" -> ChatFormatting.GOLD + "[Gold] " + ChatFormatting.RESET;
            case "gray" -> ChatFormatting.GRAY + "[Gray] " + ChatFormatting.RESET;
            case "dark_gray" -> ChatFormatting.DARK_GRAY + "[Dark Gray] " + ChatFormatting.RESET;
            case "blue" -> ChatFormatting.BLUE + "[Blue] " + ChatFormatting.RESET;
            case "green" -> ChatFormatting.GREEN + "[Green] " + ChatFormatting.RESET;
            case "aqua" -> ChatFormatting.AQUA + "[Aqua] " + ChatFormatting.RESET;
            case "red" -> ChatFormatting.RED + "[Red] " + ChatFormatting.RESET;
            case "light_purple" -> ChatFormatting.LIGHT_PURPLE + "[Light Purple] " + ChatFormatting.RESET;
            case "yellow" -> ChatFormatting.YELLOW + "[Yellow] " + ChatFormatting.RESET;
            case "white" -> ChatFormatting.WHITE + "[White] " + ChatFormatting.RESET;
            default -> "";
        };
    }
}
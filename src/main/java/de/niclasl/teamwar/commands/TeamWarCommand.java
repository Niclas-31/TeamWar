package de.niclasl.teamwar.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.bed.BedManager;
import de.niclasl.teamwar.border.BorderManager;
import de.niclasl.teamwar.event.BlockBreakReward;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameState;
import de.niclasl.teamwar.lobby.LobbyManager;
import de.niclasl.teamwar.pvp.PvPManager;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamLocation;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class TeamWarCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("teamwar")
                        .then(Commands.literal("create")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("team", StringArgumentType.word())
                                        .executes(context -> {
                                            String team =
                                                    StringArgumentType.getString(context, "team");
                                            create(context.getSource(), team);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("delete")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .then(Commands.argument("team", TeamArgument.team())
                                        .executes(context -> {
                                            PlayerTeam team =
                                                    TeamArgument.getTeam(context, "team");
                                            delete(context.getSource(), team.getName());
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.literal("spawn")
                                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                        .then(Commands.argument("team", TeamArgument.team())
                                                .then(Commands.argument("world", DimensionArgument.dimension())
                                                        .executes(context -> {
                                                            PlayerTeam team =
                                                                    TeamArgument.getTeam(context, "team");
                                                            ServerLevel level =
                                                                    DimensionArgument.getDimension(context, "world");
                                                            setSpawn(context.getSource(), team.getName(), level);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("bed")
                                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                        .then(Commands.argument("team", TeamArgument.team())
                                                .then(Commands.argument("world", DimensionArgument.dimension())
                                                        .executes(context -> {
                                                            PlayerTeam team =
                                                                    TeamArgument.getTeam(context, "team");
                                                            ServerLevel level =
                                                                    DimensionArgument.getDimension(context, "world");
                                                            setBed(context.getSource(), team.getName(), level);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("base")
                                        .then(Commands.literal("pos1")
                                                .then(Commands.argument("position", BlockPosArgument.blockPos())
                                                        .executes(context -> {
                                                            BlockPos pos =
                                                                    BlockPosArgument.getBlockPos(context, "position");

                                                            setBase(context.getSource(), pos, true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("pos2")
                                                .then(Commands.argument("position", BlockPosArgument.blockPos())
                                                        .executes(context -> {
                                                            BlockPos pos =
                                                                    BlockPosArgument.getBlockPos(context, "position");

                                                            setBase(context.getSource(), pos, false);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("lobby")
                                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                        .then(Commands.argument("world", DimensionArgument.dimension())
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(context -> {
                                                            ServerLevel level =
                                                                    DimensionArgument.getDimension(context, "world");
                                                            BlockPos pos =
                                                                    BlockPosArgument.getBlockPos(context, "pos");
                                                            setLobby(context.getSource(), pos, level);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("lobby")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> {
                                    lobby(context.getSource());
                                    return 1;
                                })
                        )
                        .then(Commands.literal("join")
                                .then(Commands.argument("team", TeamArgument.team())
                                        .executes(context -> {
                                            PlayerTeam team =
                                                    TeamArgument.getTeam(context, "team");
                                            join(context.getSource(), team.getName());
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("leave")
                                .executes(context -> {
                                    leave(context.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("viewer")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer player =
                                                            EntityArgument.getPlayer(ctx, "player");
                                                    viewer(player, true);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer player =
                                                            EntityArgument.getPlayer(ctx, "player");
                                                    viewer(player, false);
                                                    return 1;
                                                })
                                        ))
                        )
                        .then(Commands.literal("start")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> {
                                    start(context.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("stop")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> {
                                    stop(context.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("status")
                                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                .executes(context -> {
                                    status(context.getSource());
                                    return 1;
                                }))
        );
    }

    private static void create(CommandSourceStack source, String teamName) {
        TeamManager teamManager = TeamWar.getTeamManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (teamManager.exists(teamName)) {
            player.sendSystemMessage(Component.literal("§cTeam already exists").withStyle(ChatFormatting.RED));
            return;
        }

        teamManager.createTeam(teamName);
        player.sendSystemMessage(Component.literal("Team created: " + teamName).withStyle(ChatFormatting.GREEN));
    }

    private static void delete(CommandSourceStack source, String teamName) {
        TeamManager teamManager = TeamWar.getTeamManager();
        BedManager bedManager = TeamWar.getBedManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!teamManager.exists(teamName)) {
            player.sendSystemMessage(Component.literal("Team doesn't exist!").withStyle(ChatFormatting.RED));
            return;
        }

        teamManager.deleteTeam(teamName);
        bedManager.removeBed(teamName);
        bedManager.removeRespawnForTeam(teamName);
        player.sendSystemMessage(Component.literal("Team deleted: " + teamName).withStyle(ChatFormatting.GREEN));
    }

    private static void setSpawn(CommandSourceStack source, String teamName, ServerLevel level) {
        TeamManager teamManager = TeamWar.getTeamManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!teamManager.exists(teamName)) {
            player.sendSystemMessage(Component.literal("Team doesn't exist!").withStyle(ChatFormatting.RED));
            return;
        }

        String world = level.dimension().identifier().toString();

        TeamLocation loc = new TeamLocation(
                world,
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYHeadRot(),
                player.getXRot()
        );
        Team team = teamManager.getTeamByName(teamName);
        if (team != null) team.setSpawn(loc);
        teamManager.setTeamSpawn(teamName, loc);
        player.sendSystemMessage(Component.literal("Spawn set for team " + teamName).withStyle(ChatFormatting.GREEN));
    }

    private static void setBed(CommandSourceStack source, String teamName, ServerLevel level) {
        TeamManager teamManager = TeamWar.getTeamManager();
        BedManager bedManager = TeamWar.getBedManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!teamManager.exists(teamName)) {
            player.sendSystemMessage(Component.literal("Team doesn't exist!").withStyle(ChatFormatting.RED));
            return;
        }

        BlockPos base = player.getOnPos();

        Direction facing = bedManager.getFacingFromYaw(player.getYHeadRot());

        BlockPos head = base.relative(facing);

        bedManager.setBedForTeam(teamName, level, base, head, player.getYHeadRot());

        player.sendSystemMessage(Component.literal("Bed set for team " + teamName +
                " at X:" + base.getX() +
                " Y:" + base.getY() +
                " Z:" + base.getZ())
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void setBase(CommandSourceStack source, BlockPos pos, boolean isPos1) {
        TeamManager teamManager = TeamWar.getTeamManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        Team team = teamManager.getTeam(player.getUUID());
        if (team == null) {
            player.sendSystemMessage(Component.literal("You're not on any team!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (isPos1) {
            teamManager.setPos1(team, pos);
            player.sendSystemMessage(Component.literal(
                    "Base point 1 set at: X=" + pos.getX() +
                    " Y=" + pos.getY() +
                    " Z=" + pos.getZ())
                    .withStyle(ChatFormatting.GREEN)
            );
        } else {
            teamManager.setPos2(team, pos);
            player.sendSystemMessage(Component.literal(
                    "Base point 2 set at: X=" + pos.getX() +
                    " Y=" + pos.getY() +
                    " Z=" + pos.getZ())
                    .withStyle(ChatFormatting.GREEN)
            );
        }

        if (team.getPos1() != null && team.getPos2() != null) {
            player.sendSystemMessage(Component.literal("Team base has been set from: " +
                    team.getPos1().getX() + " " + team.getPos1().getY() + " " + team.getPos1().getY() +
                    " to: " +
                    team.getPos2().getX() + " " + team.getPos2().getY() + " " + team.getPos2().getY())
                    .withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    private static void setLobby(CommandSourceStack source, BlockPos pos, ServerLevel level) {
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        lobbyManager.setLobbySpawn(level, pos, player.getYHeadRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("Lobby spawn set!")
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void lobby(CommandSourceStack source) {
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        GameManager gameManager = TeamWar.getGameManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (lobbyManager.getLobbySpawn() == null) {
            player.sendSystemMessage(Component.literal("Lobby spawn is not set yet!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }


        gameManager.setGameState(GameState.LOBBY);
        player.sendSystemMessage(Component.literal("All players have been teleported to the lobby!")
                .withStyle(ChatFormatting.GREEN)
        );
        lobbyManager.addPlayerToLobby(player);

        lobbyManager.teleportAllToLobby();
    }

    private static void join(CommandSourceStack source, String teamName) {
        TeamManager teamManager = TeamWar.getTeamManager();
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (lobbyManager.isInLobby(player)) {
            player.sendSystemMessage(Component.literal(
                    "§eYou are in the game and cannot use this command!"
            ).withStyle(ChatFormatting.RED));
            return;
        }

        if (player.level().getServer().getPlayerList().isOp(player.nameAndId())) {
            player.sendSystemMessage(Component.literal("Admins cannot join a team!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!teamManager.exists(teamName)) {
            player.sendSystemMessage(Component.literal("Team doesn't exist!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        int maxPlayers = 5;
        if (teamManager.getTeamPlayers(teamName).size() >= maxPlayers) {
            player.sendSystemMessage(Component.literal(
                    "This team is full! Max " + maxPlayers + " players allowed.")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        teamManager.addPlayerToTeam(player.getUUID(), teamName);
        player.sendSystemMessage(Component.literal("You joined team: " + teamName)
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void leave(CommandSourceStack source) {
        TeamManager teamManager = TeamWar.getTeamManager();
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (lobbyManager.isInLobby(player)) {
            player.sendSystemMessage(Component.literal(
                    "§eYou are in the game and cannot use this command!"
                    ).withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (!teamManager.isInTeam(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are not in any team!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        Team team = teamManager.getTeam(player.getUUID().toString());
        if (team != null) {
            teamManager.removePlayerFromTeam(player.getUUID());
        }

        player.sendSystemMessage(Component.literal("You left your team")
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void viewer(ServerPlayer player, boolean add) {
        TeamManager teamManager = TeamWar.getTeamManager();

        if (add) {
            teamManager.addViewers(player.getUUID());
        } else {
            teamManager.removeViewers(player.getUUID());
        }
    }

    private static void start(CommandSourceStack source) {
        TeamManager teamManager = TeamWar.getTeamManager();
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        GameManager gameManager = TeamWar.getGameManager();
        BedManager bedManager = TeamWar.getBedManager();
        PvPManager pvpManager = TeamWar.getPvPManager();
        BorderManager borderManager = TeamWar.getBorderManager();
        ServerPlayer commandPlayer;

        try {
            commandPlayer = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (GameManager.getGameState() == GameState.RUNNING) {
            commandPlayer.sendSystemMessage(Component.literal("The game is already running!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        gameManager.setGameState(GameState.RUNNING);

        for (UUID uuid : teamManager.getPlayerTeams().keySet()) {

            ServerPlayer player = commandPlayer.level().getServer().getPlayerList().getPlayer(uuid);

            if (player == null) {
                continue;
            }

            String teamName = teamManager.getPlayerTeams().get(uuid);

            if (teamName == null) {
                continue;
            }

            TeamLocation spawnLocation = teamManager.getTeamSpawns().get(teamName.toLowerCase(Locale.ROOT));

            if (spawnLocation != null) {

                ServerLevel level =
                        commandPlayer.level().getServer().getLevel(
                                ResourceKey.create(
                                        Registries.DIMENSION,
                                        Identifier.parse(spawnLocation.world)
                                )
                        );

                if (level != null) {
                    player.teleportTo(
                            level,
                            spawnLocation.x,
                            spawnLocation.y,
                            spawnLocation.z,
                            Set.of(),
                            spawnLocation.yaw,
                            spawnLocation.pitch,
                            false
                    );
                }
            }
            lobbyManager.removePlayerFromLobby(player);
            player.sendSystemMessage(Component.literal("You have been teleported to your team spawn.")
                    .withStyle(ChatFormatting.GREEN));
        }

        commandPlayer.sendSystemMessage(Component.literal("The game has started!")
                .withStyle(ChatFormatting.GREEN)
        );

        pvpManager.startGame();
        borderManager.startGameBorder();
        bedManager.spawnTeamBeds();
    }

    private static void stop(CommandSourceStack source) {
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        GameManager gameManager = TeamWar.getGameManager();
        BedManager bedManager = TeamWar.getBedManager();
        PvPManager pvpManager = TeamWar.getPvPManager();
        BorderManager borderManager = TeamWar.getBorderManager();

        ServerPlayer commandPlayer;

        try {
            commandPlayer = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (GameManager.getGameState() == GameState.LOBBY
                || GameManager.getGameState() == GameState.ENDED) {
            commandPlayer.sendSystemMessage(Component.literal("The game is already stopped!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        if (lobbyManager.getLobbySpawn() == null) {
            commandPlayer.sendSystemMessage(Component.literal("Lobby spawn is not set!")
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        commandPlayer.sendSystemMessage(Component.literal("Stopping the game...")
                .withStyle(ChatFormatting.RED)
        );

        gameManager.setGameState(GameState.ENDED);

        pvpManager.resetGame();
        bedManager.removeAllBeds();
        BlockBreakReward.resetBlocks();

        for (ServerPlayer player : commandPlayer.level().getServer().getPlayerList().getPlayers()) {
            lobbyManager.addPlayerToLobby(player);
        }

        lobbyManager.teleportAllToLobby();

        gameManager.setGameState(GameState.LOBBY);
        borderManager.stopGameBorder();

        commandPlayer.level().getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("The game has been stopped!")
                        .withStyle(ChatFormatting.RED), false
        );
    }

    private static void status(CommandSourceStack source) {
        TeamManager teamManager = TeamWar.getTeamManager();
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(
                    Component.literal("Only players can use this command!")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        player.sendSystemMessage(Component.literal("Teams: " +
                String.join(", ", teamManager.getTeams().keySet()))
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Team Spawns: " +
                String.join(", ", teamManager.getTeamSpawns().keySet()))
                .withStyle(ChatFormatting.GREEN));
    }
}
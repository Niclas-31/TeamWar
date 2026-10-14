package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameState;
import de.niclasl.teamwar.player.PlayerManager;
import de.niclasl.teamwar.team.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Locale;
import java.util.UUID;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerJoin {

    private static final int MAX_DAILY_SECONDS = 60;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (GameManager.getGameState() == GameState.RUNNING) {
            return;
        }

        PlayerManager playerManager = TeamWar.getPlayerManager();

        UUID uuid = player.getUUID();

        int playedSeconds = playerManager.getDailyPlayTime(uuid);

        if (playedSeconds >= MAX_DAILY_SECONDS) {
            player.connection.disconnect(
                    Component.literal(
                            "You have reached your 2 hours of playtime today!"
                    ).withStyle(ChatFormatting.RED)
            );
            return;
        }

        player.sendSystemMessage(
                Component.literal(
                        "Week "
                                + TeamWar.getPvPManager().getCurrentWeek()
                                + "/6  "
                                + "Day "
                                + TeamWar.getPvPManager().getCurrentDay()
                                + "/7"
                ).withStyle(ChatFormatting.YELLOW),
                true
        );
    }

    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MinecraftServer server = player.level().getServer();

        server.execute(() -> {
            String teamName =
                    TeamWar.getTeamManager()
                            .getTeamOfPlayer(player.getUUID());

            Team team = teamName != null
                    ? TeamWar.getTeamManager()
                    .getTeams()
                    .get(teamName.toLowerCase(Locale.ROOT))
                    : null;

            if (team == null) {
                setSpectator(player);

                server.getPlayerList().broadcastSystemMessage(
                        Component.literal(
                                player.getName().getString()
                                        + " is eliminated! (No team)"
                        ).withStyle(ChatFormatting.RED),
                        false
                );
                return;
            }

            if (!team.isBedAlive()) {
                setSpectator(player);

                server.getPlayerList().broadcastSystemMessage(
                        Component.literal(
                                player.getName().getString()
                                        + " is eliminated! (Bed destroyed)"
                        ).withStyle(ChatFormatting.RED),
                        false
                );
                return;
            }

            player.sendSystemMessage(
                    Component.literal(
                            "You have respawned again!"
                    ).withStyle(ChatFormatting.GREEN)
            );
        });
    }

    private static void setSpectator(ServerPlayer player) {
        player.setGameMode(GameType.SPECTATOR);

        player.getInventory().clearContent();

        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);

        player.sendSystemMessage(
                Component.literal(
                        "You are eliminated and now a spectator!"
                ).withStyle(ChatFormatting.GRAY)
        );
    }
}
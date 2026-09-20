package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.bed.BedSavedData;
import de.niclasl.teamwar.classes.PlayerUpgradeSavedData;
import de.niclasl.teamwar.economy.MoneySavedData;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.game.GameSavedData;
import de.niclasl.teamwar.game.GameState;
import de.niclasl.teamwar.lobby.LobbyManager;
import de.niclasl.teamwar.player.PlayerManager;
import de.niclasl.teamwar.player.PlayerSavedData;
import de.niclasl.teamwar.scoreboard.TeamWarScoreboard;
import de.niclasl.teamwar.team.TeamSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerManager playerManager = TeamWar.getPlayerManager();
        LobbyManager lobbyManager = TeamWar.getLobbyManager();
        TeamWarScoreboard scoreboard = TeamWar.getScoreboard();

        playerManager.playerJoin(player);

        if (GameManager.getGameState() == GameState.LOBBY) {
            BlockPos lobby = lobbyManager.getLobbySpawn();

            if (lobby == null) {
                return;
            }

            lobbyManager.teleportToLobby(player);
            lobbyManager.addPlayerToLobby(player);
            scoreboard.createScoreboard();
        }
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerManager playerManager = TeamWar.getPlayerManager();
        LobbyManager lobbyManager = TeamWar.getLobbyManager();

        playerManager.playerQuit(player);

        if (GameManager.getGameState() == GameState.LOBBY) {
            lobbyManager.removePlayerFromLobby(player);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BedSavedData bed = BedSavedData.get(level.getServer().overworld());

        if (bed.isDirty()) {
            level.getDataStorage().saveAndJoin();
            bed.setDirty(false);
        }

        PlayerUpgradeSavedData upgrade = PlayerUpgradeSavedData.get(level.getServer().overworld());

        if (upgrade.isDirty()) {
            level.getDataStorage().saveAndJoin();
            upgrade.setDirty(false);
        }

        MoneySavedData money = MoneySavedData.get(level.getServer().overworld());

        if (money.isDirty()) {
            level.getDataStorage().saveAndJoin();
            money.setDirty(false);
        }

        GameSavedData game = GameSavedData.get(level.getServer().overworld());

        if (game.isDirty()) {
            level.getDataStorage().saveAndJoin();
            game.setDirty(false);
        }

        PlayerSavedData player = PlayerSavedData.get(level.getServer().overworld());

        if (player.isDirty()) {
            level.getDataStorage().saveAndJoin();
            player.setDirty(false);
        }

        TeamSavedData team = TeamSavedData.get(level.getServer().overworld());

        if (team.isDirty()) {
            level.getDataStorage().saveAndJoin();
            team.setDirty(false);
        }
    }
}
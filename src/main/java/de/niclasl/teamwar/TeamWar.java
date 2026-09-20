package de.niclasl.teamwar;

import com.mojang.logging.LogUtils;
import de.niclasl.teamwar.bed.BedManager;
import de.niclasl.teamwar.border.BorderManager;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.economy.MoneyManager;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.lobby.LobbyManager;
import de.niclasl.teamwar.menu.ModMenuTypes;
import de.niclasl.teamwar.player.PlayerManager;
import de.niclasl.teamwar.pvp.PvPManager;
import de.niclasl.teamwar.scoreboard.TeamWarScoreboard;
import de.niclasl.teamwar.strength.StrengthManager;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(TeamWar.MOD_ID)
public class TeamWar {
    public static final String MOD_ID = "teamwar";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static TeamManager teamManager;
    private static PlayerManager playerManager;
    private static GameManager gameManager;
    private static LobbyManager lobbyManager;
    private static BedManager bedManager;
    private static PvPManager pvpManager;
    private static BorderManager borderManager;
    private static ClassManager classManager;
    private static StrengthManager strengthManager;
    private static TeamWarScoreboard scoreboard;
    private static MoneyManager moneyManager;

    public TeamWar(IEventBus modEventBus, ModContainer modContainer) {

        NeoForge.EVENT_BUS.register(this);

        ModMenuTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        teamManager = new TeamManager(server);
        playerManager = new PlayerManager(server, 7200);
        gameManager = new GameManager(server);
        lobbyManager = new LobbyManager(server);
        bedManager = new BedManager(server);
        pvpManager = new PvPManager(server);
        borderManager = new BorderManager(server);
        classManager = new ClassManager(server);
        strengthManager = new StrengthManager(server);
        scoreboard = new TeamWarScoreboard(server);
        moneyManager = new MoneyManager(server);
    }

    public static TeamManager getTeamManager() {
        return teamManager;
    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    public static LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public static BedManager getBedManager() {
        return bedManager;
    }

    public static PvPManager getPvPManager() {
        return pvpManager;
    }

    public static BorderManager getBorderManager() {
        return borderManager;
    }

    public static ClassManager getClassManager() {
        return classManager;
    }

    public static StrengthManager getStrengthManager() {
        return strengthManager;
    }

    public static TeamWarScoreboard getScoreboard() {
        return scoreboard;
    }

    public static MoneyManager getMoneyManager() {
        return moneyManager;
    }
}
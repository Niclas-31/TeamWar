package de.niclasl.teamwar;

import com.mojang.logging.LogUtils;
import de.niclasl.teamwar.api.events.EventScheduler;
import de.niclasl.teamwar.api.events.Events;
import de.niclasl.teamwar.bed.BedManager;
import de.niclasl.teamwar.border.BorderManager;
import de.niclasl.teamwar.classes.ClassManager;
import de.niclasl.teamwar.economy.MoneyManager;
import de.niclasl.teamwar.game.GameManager;
import de.niclasl.teamwar.lobby.LobbyManager;
import de.niclasl.teamwar.menu.ModMenuTypes;
import de.niclasl.teamwar.network.ModNetwork;
import de.niclasl.teamwar.player.PlayerManager;
import de.niclasl.teamwar.pvp.PvPManager;
import de.niclasl.teamwar.registries.blocks.ModBlocks;
import de.niclasl.teamwar.registries.items.ModItems;
import de.niclasl.teamwar.repair.WorldBalanceSavedData;
import de.niclasl.teamwar.repair.WorldBalanceState;
import de.niclasl.teamwar.scoreboard.TeamWarScoreboard;
import de.niclasl.teamwar.strength.StrengthManager;
import de.niclasl.teamwar.team.TeamManager;
import de.niclasl.teamwar.worldgen.biome_source.ModBiomeSource;
import de.niclasl.teamwar.worldgen.density.IslandDensityFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

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

    private static final String REPAIR_FILE = "overworld_repair.dat";

    public TeamWar(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::register);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(EventScheduler::onServerTick);

        IslandDensityFunction.init();
        IslandDensityFunction.REGISTER.register(modEventBus);
        ModBiomeSource.init();
        ModBiomeSource.REGISTER.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        Events.register();

        ModMenuTypes.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CORRUPTED_PICKAXE);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CORRUPTED_COMMAND_BLOCK);
        }
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

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        try {
            Path overworldPath = server.getWorldPath(LevelResource.ROOT)
                    .resolve("dimensions")
                    .resolve("minecraft")
                    .resolve("overworld");

            UUID currentIdentity = getOrCreateWorldIdentity(overworldPath);

            WorldBalanceSavedData data = server.getDataStorage().computeIfAbsent(WorldBalanceSavedData.TYPE);

            if (isOverworldRegenerated(server, currentIdentity)) {

                data.setState(WorldBalanceState.BALANCED);
                data.setDirty();

                LOGGER.info("Neue Oberwelt erkannt. Weltzustand auf BALANCED gesetzt.");
                return;
            }

            if (data.getState() == WorldBalanceState.INCOMPATIBLE) {
                LOGGER.error("Oberwelt wurde nicht als neu generiert bestätigt. Server wird gestoppt.");
                server.halt(false);
            }

        } catch (IOException exception) {
            LOGGER.error("Fehler bei der Überprüfung der Oberwelt.", exception);
            server.halt(false);
        }
    }

    private static boolean isOverworldRegenerated(MinecraftServer server, UUID currentIdentity) throws IOException {

        Path repairFile = server.getServerDirectory()
                .resolve("config")
                .resolve(MOD_ID)
                .resolve(REPAIR_FILE);

        if (!Files.exists(repairFile)) {
            return false;
        }

        String previousValue = Files.readString(repairFile).trim();

        UUID previousIdentity;

        try {
            previousIdentity = UUID.fromString(previousValue);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Ungültige gespeicherte Oberwelt-ID.", exception);
        }

        if (previousIdentity.equals(currentIdentity)) {
            return false;
        }

        Files.delete(repairFile);
        return true;
    }

    private static UUID getOrCreateWorldIdentity(Path overworldPath)
            throws IOException {

        Path identityPath = overworldPath.resolve("world_identity.dat");

        if (Files.exists(identityPath)) {
            String value = Files.readString(identityPath).trim();
            return UUID.fromString(value);
        }

        UUID newIdentity = UUID.randomUUID();

        Files.writeString(
                identityPath,
                newIdentity.toString(),
                StandardOpenOption.CREATE_NEW
        );

        return newIdentity;
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
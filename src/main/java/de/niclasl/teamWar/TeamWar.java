package de.niclasl.teamWar;

import de.niclasl.teamWar.classes.PlayerClass;
import de.niclasl.teamWar.classes.command.ClassCommand;
import de.niclasl.teamWar.classes.command.HealCommand;
import de.niclasl.teamWar.classes.manager.PlayerUpgrades;
import de.niclasl.teamWar.classes.command.UpgradeCommand;
import de.niclasl.teamWar.classes.listener.UpgradeGUIListener;
import de.niclasl.teamWar.classes.manager.ClassManager;
import de.niclasl.teamWar.enviroment.EnvironmentManager;
import de.niclasl.teamWar.market.command.MarketCommand;
import de.niclasl.teamWar.money.TeamWarEconomy;
import de.niclasl.teamWar.money.command.MoneyCommand;
import de.niclasl.teamWar.money.command.PayCommand;
import de.niclasl.teamWar.teamwar.commands.PlayTimeCommand;
import de.niclasl.teamWar.teamwar.commands.PlayerStatsCommand;
import de.niclasl.teamWar.teamwar.commands.TeamColorsCommand;
import de.niclasl.teamWar.teamwar.commands.TeamWarCommand;
import de.niclasl.teamWar.market.gui.MarketMainGui;
import de.niclasl.teamWar.market.manager.MarketManager;
import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.listener.*;
import de.niclasl.teamWar.teamwar.manager.*;
import de.niclasl.teamWar.scoreboard.TeamWarScoreboard;
import de.niclasl.teamWar.money.listener.BlockBreakRewardListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeamWar extends JavaPlugin implements Listener {

    private TeamWarScoreboard scoreboard;
    private static TeamWar instance;

    private final LobbyManager lobbyManager = new LobbyManager(this);
    private final BorderManager borderManager = new BorderManager(this);
    private final PlayerTimeManager playerTimeManager = new PlayerTimeManager(7200, this);
    private final MarketManager marketManager = new MarketManager(this);

    private boolean scoreboardLayout = true;

    private static final Map<String, ChatColor> teamColors = Map.ofEntries(
            Map.entry("black", ChatColor.BLACK),
            Map.entry("dark_blue", ChatColor.DARK_BLUE),
            Map.entry("dark_green", ChatColor.DARK_GREEN),
            Map.entry("dark_aqua", ChatColor.DARK_AQUA),
            Map.entry("dark_red", ChatColor.DARK_RED),
            Map.entry("dark_purple", ChatColor.DARK_PURPLE),
            Map.entry("gold", ChatColor.GOLD),
            Map.entry("gray", ChatColor.GRAY),
            Map.entry("dark_gray", ChatColor.DARK_GRAY),
            Map.entry("blue", ChatColor.BLUE),
            Map.entry("green", ChatColor.GREEN),
            Map.entry("aqua", ChatColor.AQUA),
            Map.entry("red", ChatColor.RED),
            Map.entry("light_purple", ChatColor.LIGHT_PURPLE),
            Map.entry("yellow", ChatColor.YELLOW),
            Map.entry("white", ChatColor.WHITE)
    );

    @Override
    public void onEnable() {

        if (setupEconomy()) {
            getLogger().warning("No Vault-compatible economy found! Economy features will be disabled.");
        }

        instance = this;

        saveDefaultConfig();

        TeamManager teamManager = new TeamManager(this);
        BedManager bedManager = new BedManager(this);
        GameStateManager gameStateManager = new GameStateManager(this);
        TeamWarManager teamWarManager = new TeamWarManager(this);
        MoneyManager moneyManager = new MoneyManager(this);
        ClassManager classManager = new ClassManager();
        TeamWarEconomy teamWarEconomy = new TeamWarEconomy();

        if (!gameStateManager.isGameRunning()) {
            Bukkit.getWorlds().getFirst().getWorldBorder().setSize(35);
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getServer().getServicesManager().register(Economy.class, teamWarEconomy, this, ServicePriority.Normal);
        }

        TeamManager.loadAllTeams();

        PvPManager.init(this);

        MarketMainGui marketMainGui = new MarketMainGui(this, marketManager);

        scoreboard = new TeamWarScoreboard(this);
        EnvironmentManager.startEnvironmentTask();

        new PrefixUpdater().runTaskTimer(this, 20L, 20L);

        teamWarManager.load();
        PlayerUpgrades.loadUpgrades();
        bedManager.loadAllBeds();
        startScoreboardUpdater();
        startTabAndChatFormat();

        Map<UUID, Integer> dailyPlaySeconds = new HashMap<>();

        getServer().getPluginManager().registerEvents(marketMainGui, this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameStateManager, dailyPlaySeconds, this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(bedManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakRewardListener(), this);
        getServer().getPluginManager().registerEvents(new BaseProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new EnvironmentManager(), this);
        getServer().getPluginManager().registerEvents(new ForbiddenItems(), this);
        getServer().getPluginManager().registerEvents(new UpgradeGUIListener(), this);
        getServer().getPluginManager().registerEvents(new PotionConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new OnJoin(), this);
        getServer().getPluginManager().registerEvents(new Blocker(), this);
        getServer().getPluginManager().registerEvents(new PlayerTimeManager(7200, this), this);

        Objects.requireNonNull(getCommand("teamwar")).setExecutor(new TeamWarCommand(teamManager, gameStateManager, bedManager, this));
        Objects.requireNonNull(getCommand("teamwar")).setTabCompleter(new TeamWarCommand(teamManager, gameStateManager, bedManager, this));
        Objects.requireNonNull(getCommand("playtime")).setExecutor(new PlayTimeCommand());
        Objects.requireNonNull(getCommand("playtime")).setTabCompleter(new PlayTimeCommand());
        Objects.requireNonNull(getCommand("market")).setExecutor(new MarketCommand(marketManager));
        Objects.requireNonNull(getCommand("market")).setTabCompleter(new MarketCommand(marketManager));
        Objects.requireNonNull(getCommand("money")).setExecutor(new MoneyCommand(moneyManager));
        Objects.requireNonNull(getCommand("money")).setTabCompleter(new MoneyCommand(moneyManager));
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand());
        Objects.requireNonNull(getCommand("pay")).setTabCompleter(new PayCommand());
        Objects.requireNonNull(getCommand("class")).setExecutor(new ClassCommand(classManager));
        Objects.requireNonNull(getCommand("class")).setTabCompleter(new ClassCommand(classManager));
        Objects.requireNonNull(getCommand("heal")).setExecutor(new HealCommand());
        Objects.requireNonNull(getCommand("heal")).setTabCompleter(new HealCommand());
        Objects.requireNonNull(getCommand("upgrade")).setExecutor(new UpgradeCommand());
        Objects.requireNonNull(getCommand("upgrade")).setTabCompleter(new UpgradeCommand());
        Objects.requireNonNull(getCommand("playerstats")).setExecutor(new PlayerStatsCommand());
        Objects.requireNonNull(getCommand("playerstats")).setTabCompleter(new PlayerStatsCommand());
        Objects.requireNonNull(getCommand("team-colors")).setExecutor(new TeamColorsCommand());
        Objects.requireNonNull(getCommand("team-colors")).setTabCompleter(new TeamColorsCommand());

        PlayerTimeManager.startDailyTimer();
        getLogger().info("TeamWar enabled");
    }

    public static TeamWar getInstance() {
        return instance;
    }

    public TeamWarScoreboard getScoreboardManager() {
        return scoreboard;
    }

    private boolean setupEconomy() {
        return getServer().getPluginManager().getPlugin("Vault") != null
                && Bukkit.getServicesManager().getRegistration(Economy.class) != null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        TeamWar plugin = JavaPlugin.getPlugin(TeamWar.class);
        plugin.getScoreboardManager().createScoreboard(e.getPlayer());
    }

    public void startTabAndChatFormat() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerTabStats(player);
                }
            }
        }.runTaskTimer(TeamWar.getInstance(), 0L, 20L);
    }

    public void startScoreboardUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {

                    if (TeamWarScoreboard.getPlayerScoreboard(p) == null) {
                        scoreboard.createScoreboard(p);
                    }

                    scoreboard.updateScoreboard(p, scoreboardLayout);
                }
                scoreboardLayout = !scoreboardLayout;
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    public static void updatePlayerTabStats(Player player) {
        String prefix = "";
        String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
        if (teamName != null && TeamManager.exists(teamName)) {
            prefix = getTeamPrefix(teamName);
        } else if (player.isOp()) {
            prefix = ChatColor.DARK_RED + "[Admin] " + ChatColor.RESET;
        }

        player.setDisplayName(prefix + player.getName());

        PlayerClass pc = ClassManager.getClass(player);
        String baseName = prefix + player.getName();
        if (pc != null) {
            player.setPlayerListName(baseName);
        }
    }

    public static String getTeamPrefix(String teamName) {
        ChatColor color = teamColors.getOrDefault(teamName.toLowerCase(), ChatColor.WHITE);

        String displayName = teamName.substring(0, 1).toUpperCase() + teamName.substring(1).toLowerCase();
        return color + "[" + displayName + "] " + ChatColor.RESET;
    }

    @Override
    public void onDisable() {
        MoneyManager.saveAll();
        getLogger().info("TeamWar disabled");
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public BorderManager getBorderManager() {
        return borderManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }
}
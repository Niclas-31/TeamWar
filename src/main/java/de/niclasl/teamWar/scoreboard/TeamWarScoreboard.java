package de.niclasl.teamWar.scoreboard;

import de.niclasl.teamWar.TeamWar;
import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.strength.manager.StrengthManager;
import de.niclasl.teamWar.teamwar.manager.PvPManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import de.niclasl.teamWar.teamwar.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class TeamWarScoreboard implements Listener {

    private static final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();

    private final TeamWar plugin;
    private boolean toggle = false;

    private static final ChatColor[] UNIQUE_COLORS = {
            ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA,
            ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
            ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
            ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
    };

    public TeamWarScoreboard(TeamWar plugin) {
        this.plugin = plugin;
        startSwitching();
    }

    public void createScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("teamwar", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "TeamWar");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        playerScoreboards.put(player, board);
    }

    public void updateScoreboard(Player player, boolean layout1) {
        setScoreboard(player, layout1);
    }

    private void setScoreboard(Player player, boolean layout1) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("teamwar", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + (layout1 ? "TeamWar" : "TeamWar Info"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = 15;

        if (layout1) {
            obj.getScore(unique(ChatColor.YELLOW + "--- Teams ---", score)).setScore(score--);

            List<Team> teams = new ArrayList<>(TeamManager.getTeams().values());

            int reservedSlots = 4;

            teams.sort((a, b) -> {
                if (a.isBedAlive() && !b.isBedAlive()) return -1;
                if (!a.isBedAlive() && b.isBedAlive()) return 1;
                return Integer.compare(b.getMembers().size(), a.getMembers().size());
            });

            int shown = 0;
            for (Team t : teams) {
                if (shown >= reservedSlots) break;

                boolean bedAlive = t.isBedAlive();
                int players = t.getMembers().size();

                String line = getTeamColor(t.getName()) + t.getName() + ": " + teamStatus(bedAlive, players);
                obj.getScore(unique(line, score)).setScore(score);
                score--;
                shown++;
            }

            obj.getScore(unique(ChatColor.YELLOW + "--- Top Teams ---", score)).setScore(score--);

            List<Team> topTeams = new ArrayList<>(TeamManager.getTeams().values());
            topTeams.sort((a, b) -> Integer.compare(
                    StrengthManager.getTeamStrength(b),
                    StrengthManager.getTeamStrength(a)
            ));

            int rank = 1;
            for (Team t : topTeams) {
                if (rank > 3) break;
                obj.getScore(unique(ChatColor.GOLD + "#" + rank + " " + t.getName() + ": " + StrengthManager.getTeamStrength(t) + " Strength", score)).setScore(score--);
                rank++;
            }
        } else {
            obj.getScore(unique(ChatColor.YELLOW + "--- WorldBorder ---", score)).setScore(score--);

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
            DecimalFormat format = new DecimalFormat("#,##0.##", symbols);

            double wbSize = player.getWorld().getWorldBorder().getSize();
            String formatted = format.format(wbSize);

            obj.getScore(unique(ChatColor.RED + "Worldborder: " + ChatColor.DARK_RED + formatted, score)).setScore(score--);

            obj.getScore(unique(ChatColor.YELLOW + "--- PvP ---", score)).setScore(score--);
            obj.getScore(unique(ChatColor.YELLOW + "Week: " + ChatColor.GREEN + PvPManager.getCurrentWeek(), score)).setScore(score--);
            obj.getScore(unique(ChatColor.YELLOW + "Day: " + ChatColor.GREEN + PvPManager.getCurrentDay(), score)).setScore(score--);

            String pvpTime = PvPManager.getFormattedTimeUntilPvp();
            obj.getScore(unique(ChatColor.YELLOW + "PvP: " + ChatColor.GREEN + pvpTime, score)).setScore(score--);

            String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
            String coins;
            if (teamName == null) {
                coins = MoneyManager.getPlayerMoneyFormatted(player.getUniqueId());
            } else {
                coins = MoneyManager.getTeamMoneyFormatted(teamName);
            }

            obj.getScore(unique(ChatColor.YELLOW + "--- Money ---", score)).setScore(score--);
            obj.getScore(unique(ChatColor.GREEN + coins + "$", score)).setScore(score);
        }

        player.setScoreboard(board);
        playerScoreboards.put(player, board);
    }

    private void startSwitching() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player, toggle);
                }

                toggle = !toggle;

            }
        }.runTaskTimer(plugin, 0L, 1200L);
    }

    private String teamStatus(boolean bedAlive, int players) {
        return bedAlive
                ? ChatColor.GREEN + "✅ (" + players + ")"
                : ChatColor.RED + "❌ " + ChatColor.GRAY + "(" + players + ")";
    }

    private static String unique(String text, int score) {
        String visible = ChatColor.stripColor(text);
        if (visible.length() > 34) {
            text = text.substring(0, Math.min(text.length(), 34));
        }

        int idx = Math.floorMod(score, UNIQUE_COLORS.length);
        return text + ChatColor.RESET + UNIQUE_COLORS[idx] + ChatColor.RESET;
    }

    private String getTeamColor(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "black" -> ChatColor.BLACK.toString();
            case "dark_blue" -> ChatColor.DARK_BLUE.toString();
            case "dark_green" -> ChatColor.DARK_GREEN.toString();
            case "dark_aqua" -> ChatColor.DARK_AQUA.toString();
            case "dark_red" -> ChatColor.DARK_RED.toString();
            case "dark_purple" -> ChatColor.DARK_PURPLE.toString();
            case "gold" -> ChatColor.GOLD.toString();
            case "gray" -> ChatColor.GRAY.toString();
            case "dark_gray" -> ChatColor.DARK_GRAY.toString();
            case "blue" -> ChatColor.BLUE.toString();
            case "green" -> ChatColor.GREEN.toString();
            case "aqua" -> ChatColor.AQUA.toString();
            case "red" -> ChatColor.RED.toString();
            case "light_purple" -> ChatColor.LIGHT_PURPLE.toString();
            case "yellow" -> ChatColor.YELLOW.toString();
            default -> ChatColor.WHITE.toString();
        };
    }

    public static Scoreboard getPlayerScoreboard(Player p) { return playerScoreboards.get(p); }
}
package de.niclasl.teamwar.scoreboard;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.economy.MoneyManager;
import de.niclasl.teamwar.pvp.PvPManager;
import de.niclasl.teamwar.strength.StrengthManager;
import de.niclasl.teamwar.team.Team;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TeamWarScoreboard {

    private final MinecraftServer server;
    private final PvPManager pvp;
    private final TeamManager team;

    private final ScheduledExecutorService scheduler;

    private boolean toggle = false;

    private static final ChatFormatting[] UNIQUE_COLORS = {
            ChatFormatting.BLACK,
            ChatFormatting.DARK_BLUE,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.DARK_RED,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.GOLD,
            ChatFormatting.GRAY,
            ChatFormatting.DARK_GRAY,
            ChatFormatting.BLUE,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.RED,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.YELLOW,
            ChatFormatting.WHITE
    };

    public TeamWarScoreboard(MinecraftServer server) {
        this.server = server;
        this.pvp = TeamWar.getPvPManager();
        this.team = TeamWar.getTeamManager();

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        switchLayout();
    }

    public void createScoreboard() {
        Scoreboard scoreboard = server.getScoreboard();

        Objective objective = scoreboard.getObjective("teamwar");

        if (objective == null) {
            objective = scoreboard.addObjective(
                    "teamwar",
                    ObjectiveCriteria.DUMMY,
                    Component.literal("TeamWar").withStyle(
                            ChatFormatting.GOLD,
                            ChatFormatting.BOLD
                    ),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null
            );
        }

        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);
    }

    public void updateScoreboard(ServerPlayer player, boolean layout1) {
        Scoreboard scoreboard = server.getScoreboard();

        Objective objective = scoreboard.getObjective("teamwar");

        if (objective == null) {
            createScoreboard();
            objective = scoreboard.getObjective("teamwar");
        }

        if (objective == null) {
            return;
        }

        clearObjective(scoreboard, objective);

        objective.setDisplayName(Component.literal(layout1 ? "TeamWar" : "TeamWar Info")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        int score = 15;

        if (layout1) {
            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW + "--- Teams ---",
                            score
                    ),
                    score--
            );

            List<Team> teams = new ArrayList<>(team.getTeams().values());

            int reservedSlots = 4;

            teams.sort((a, b) -> {
                if (a.isBedAlive() && !b.isBedAlive()) {
                    return -1;
                }

                if (!a.isBedAlive() && b.isBedAlive()) {
                    return 1;
                }

                return Integer.compare(
                        b.getMembers().size(),
                        a.getMembers().size()
                );
            });

            int shown = 0;

            for (Team team : teams) {
                if (shown >= reservedSlots) {
                    break;
                }

                boolean bedAlive = team.isBedAlive();
                int players = team.getMembers().size();

                String line =
                        getTeamColor(team.getName())
                                + team.getName()
                                + ": "
                                + teamStatus(bedAlive, players);

                addLine(
                        scoreboard,
                        objective,
                        unique(line, score),
                        score
                );

                score--;
                shown++;
            }

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW + "--- Top Teams ---",
                            score
                    ),
                    score--
            );

            List<Team> topTeams = new ArrayList<>(team.getTeams().values());
            StrengthManager strengthManager = TeamWar.getStrengthManager();

            topTeams.sort((a, b) ->
                    Integer.compare(
                            strengthManager.getTeamStrength(b),
                            strengthManager.getTeamStrength(a)
                    )
            );

            int rank = 1;

            for (Team team : topTeams) {
                if (rank > 3) {
                    break;
                }

                String line =
                        ChatFormatting.GOLD
                                + "#"
                                + rank
                                + " "
                                + team.getName()
                                + ": "
                                + strengthManager.getTeamStrength(team)
                                + " Strength";

                addLine(
                        scoreboard,
                        objective,
                        unique(line, score),
                        score--
                );

                rank++;
            }

        } else {

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW + "--- WorldBorder ---",
                            score
                    ),
                    score--
            );

            DecimalFormatSymbols symbols =
                    new DecimalFormatSymbols(Locale.GERMAN);

            DecimalFormat format =
                    new DecimalFormat("#,##0.##", symbols);

            double worldBorderSize =
                    player.level()
                            .getWorldBorder()
                            .getSize();

            String formatted =
                    format.format(worldBorderSize);

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.RED
                                    + "Worldborder: "
                                    + ChatFormatting.DARK_RED
                                    + formatted,
                            score
                    ),
                    score--
            );

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW + "--- PvP ---",
                            score
                    ),
                    score--
            );

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW
                                    + "Week: "
                                    + ChatFormatting.GREEN
                                    + pvp.getCurrentWeek(),
                            score
                    ),
                    score--
            );

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW
                                    + "Day: "
                                    + ChatFormatting.GREEN
                                    + pvp.getCurrentDay(),
                            score
                    ),
                    score--
            );

            String pvpTime = pvp.getFormattedTimeUntilPvp();

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW
                                    + "PvP: "
                                    + ChatFormatting.GREEN
                                    + pvpTime,
                            score
                    ),
                    score--
            );

            String teamName = team.getTeamOfPlayer(player.getUUID());
            MoneyManager moneyManager = TeamWar.getMoneyManager();

            String coins;

            if (teamName == null) {
                coins = moneyManager.getPlayerMoneyFormatted(player.getUUID());
            } else {
                coins = moneyManager.getTeamMoneyFormatted(teamName);
            }

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.YELLOW + "--- Money ---",
                            score
                    ),
                    score--
            );

            addLine(
                    scoreboard,
                    objective,
                    unique(
                            ChatFormatting.GREEN
                                    + coins
                                    + "$",
                            score
                    ),
                    score
            );
        }
    }

    private static void addLine(
            Scoreboard scoreboard,
            Objective objective,
            String text,
            int score
    ) {
        ScoreHolder holder = new ScoreHolder() {
            @Override
            public @NonNull String getScoreboardName() {
                return text;
            }
        };

        scoreboard.getOrCreatePlayerScore(
                holder,
                objective,
                true
        ).set(score);
    }

    private static String teamStatus(
            boolean bedAlive,
            int players
    ) {
        return bedAlive
                ? ChatFormatting.GREEN
                + "✓ ("
                + players
                + ")"
                : ChatFormatting.RED
                + "✗ "
                + ChatFormatting.GRAY
                + "("
                + players
                + ")";
    }

    private static String unique(
            String text,
            int score
    ) {
        int index =
                Math.floorMod(
                        score,
                        UNIQUE_COLORS.length
                );

        return text
                + ChatFormatting.RESET
                + UNIQUE_COLORS[index]
                + ChatFormatting.RESET;
    }

    private static String getTeamColor(String teamName) {
        return switch (teamName.toLowerCase(Locale.ROOT)) {
            case "black" ->
                    ChatFormatting.BLACK.toString();
            case "dark_blue" ->
                    ChatFormatting.DARK_BLUE.toString();
            case "dark_green" ->
                    ChatFormatting.DARK_GREEN.toString();
            case "dark_aqua" ->
                    ChatFormatting.DARK_AQUA.toString();
            case "dark_red" ->
                    ChatFormatting.DARK_RED.toString();
            case "dark_purple" ->
                    ChatFormatting.DARK_PURPLE.toString();
            case "gold" ->
                    ChatFormatting.GOLD.toString();
            case "gray" ->
                    ChatFormatting.GRAY.toString();
            case "dark_gray" ->
                    ChatFormatting.DARK_GRAY.toString();
            case "blue" ->
                    ChatFormatting.BLUE.toString();
            case "green" ->
                    ChatFormatting.GREEN.toString();
            case "aqua" ->
                    ChatFormatting.AQUA.toString();
            case "red" ->
                    ChatFormatting.RED.toString();
            case "light_purple" ->
                    ChatFormatting.LIGHT_PURPLE.toString();
            case "yellow" ->
                    ChatFormatting.YELLOW.toString();
            default ->
                    ChatFormatting.WHITE.toString();
        };
    }

    public void switchLayout() {
        scheduler.scheduleAtFixedRate(
                () -> server.execute(() -> {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        updateScoreboard(player, toggle);
                    }

                    toggle = !toggle;
                }),
                0L,
                30L,
                TimeUnit.SECONDS
        );
    }

    private void clearObjective(Scoreboard scoreboard, Objective objective) {
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            scoreboard.resetAllPlayerScores(new SidebarScoreHolder(entry.owner()));
        }
    }

    private record SidebarScoreHolder(String name) implements ScoreHolder {
        @Override
        public @NonNull String getScoreboardName() {
            return name;
        }
    }
}
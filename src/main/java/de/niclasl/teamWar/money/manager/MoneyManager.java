package de.niclasl.teamWar.money.manager;

import de.niclasl.teamWar.TeamWar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class MoneyManager {

    private static File teamFile;
    private static File playerFile;

    private static FileConfiguration teamConfig;
    private static FileConfiguration playerConfig;

    private static final Map<String, Double> teamBalances = new HashMap<>();
    private static final Map<UUID, Double> playerBalances = new HashMap<>();

    public MoneyManager(TeamWar plugin) {
        teamFile = new File(plugin.getDataFolder(), "teamMoney.yml");
        if (!teamFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                teamFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        teamConfig = YamlConfiguration.loadConfiguration(teamFile);
        if (!teamConfig.isConfigurationSection("balances")) {
            teamConfig.createSection("balances");
            saveTeamConfig();
        }
        loadTeamMoneyData();

        playerFile = new File(plugin.getDataFolder(), "money.yml");
        if (!playerFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        if (!playerConfig.isConfigurationSection("balances")) {
            playerConfig.createSection("balances");
            savePlayerConfig();
        }
        loadPlayerMoneyData();
    }

    public static double getTeamMoney(String teamName) {
        if (teamName == null) return 0;
        return teamBalances.getOrDefault(teamName.toLowerCase(), 0.0);
    }

    public static String getTeamMoneyFormatted(String teamName) {
        return formatMoney(getTeamMoney(teamName));
    }

    public static void setTeamMoney(String teamName, double amount) {
        amount = round(amount);
        teamBalances.put(teamName.toLowerCase(), amount);
        saveTeam(teamName);
    }

    public static void addTeamMoney(String teamName, double amount) {
        setTeamMoney(teamName, getTeamMoney(teamName) + amount);
    }

    public static void removeTeamMoney(String teamName, double amount) {
        double current = getTeamMoney(teamName);
        if (current >= amount) setTeamMoney(teamName, current - amount);
    }

    private static void loadTeamMoneyData() {
        teamBalances.clear();
        if (!teamConfig.isConfigurationSection("balances")) return;
        for (String team : Objects.requireNonNull(teamConfig.getConfigurationSection("balances")).getKeys(false)) {
            double amount = round(teamConfig.getDouble("balances." + team, 0.0));
            teamBalances.put(team.toLowerCase(), amount);
        }
    }

    private static void saveTeam(String teamName) {
        teamConfig.set("balances." + teamName.toLowerCase(), getTeamMoney(teamName));
        saveTeamConfig();
    }

    private static void saveTeamConfig() {
        try {
            teamConfig.save(teamFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getPlayerMoney(UUID uuid) {
        if (uuid == null) return 0;
        return playerBalances.getOrDefault(uuid, 0.0);
    }

    public static String getPlayerMoneyFormatted(UUID uuid) {
        return formatMoney(getPlayerMoney(uuid));
    }

    public static void setPlayerMoney(UUID uuid, double amount) {
        amount = round(amount);
        playerBalances.put(uuid, amount);
        savePlayer(uuid);
    }

    public static void addPlayerMoney(UUID uuid, double amount) {
        setPlayerMoney(uuid, getPlayerMoney(uuid) + amount);
    }

    public static void removePlayerMoney(UUID uuid, double amount) {
        double current = getPlayerMoney(uuid);
        if (current >= amount) setPlayerMoney(uuid, current - amount);
    }

    private static void loadPlayerMoneyData() {
        playerBalances.clear();
        if (!playerConfig.isConfigurationSection("balances")) return;
        for (String key : Objects.requireNonNull(playerConfig.getConfigurationSection("balances")).getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            double amount = round(playerConfig.getDouble("balances." + key, 0.0));
            playerBalances.put(uuid, amount);
        }
    }

    private static void savePlayer(UUID uuid) {
        playerConfig.set("balances." + uuid.toString(), getPlayerMoney(uuid));
        savePlayerConfig();
    }

    private static void savePlayerConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String formatMoney(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return df.format(amount);
    }

    public void reload() {
        teamConfig = YamlConfiguration.loadConfiguration(teamFile);
        loadTeamMoneyData();

        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        loadPlayerMoneyData();
    }

    public static void saveAll() {
        for (Map.Entry<String, Double> e : teamBalances.entrySet()) {
            teamConfig.set("balances." + e.getKey(), round(e.getValue()));
        }
        saveTeamConfig();

        for (Map.Entry<UUID, Double> e : playerBalances.entrySet()) {
            playerConfig.set("balances." + e.getKey().toString(), round(e.getValue()));
        }
        savePlayerConfig();
    }
}
package de.niclasl.teamwar.economy;

import net.minecraft.server.MinecraftServer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

public class MoneyManager {

    private final MoneySavedData data;

    public MoneyManager(MinecraftServer server) {
        this.data = MoneySavedData.get(server.overworld());
    }

    public double getTeamMoney(String teamName) {
        if (teamName == null) {
            return 0.0;
        }

        return data.teamBalances.getOrDefault(teamName.toLowerCase(Locale.ROOT), 0.0);
    }

    public String getTeamMoneyFormatted(String teamName) {
        return formatMoney(getTeamMoney(teamName));
    }

    public void setTeamMoney(String teamName, double amount) {
        if (teamName == null) {
            return;
        }

        String key = teamName.toLowerCase(Locale.ROOT);

        data.teamBalances.put(key, round(amount));

        data.setDirty();
    }

    public void addTeamMoney(String teamName, double amount) {
        setTeamMoney(teamName, getTeamMoney(teamName) + amount);
    }

    public void removeTeamMoney(String teamName, double amount) {
        double current = getTeamMoney(teamName);

        if (current >= amount) {
            setTeamMoney(teamName, current - amount);
        }
    }

    public double getPlayerMoney(UUID uuid) {
        if (uuid == null) {
            return 0.0;
        }

        return data.playerBalances.getOrDefault(uuid, 0.0);
    }

    public String getPlayerMoneyFormatted(UUID uuid) {
        return formatMoney(getPlayerMoney(uuid));
    }

    public void setPlayerMoney(UUID uuid, double amount) {
        if (uuid == null) {
            return;
        }

        data.playerBalances.put(uuid, round(amount));

        data.setDirty();
    }

    public void addPlayerMoney(UUID uuid, double amount) {
        setPlayerMoney(uuid, getPlayerMoney(uuid) + amount);
    }

    public void removePlayerMoney(UUID uuid, double amount) {
        double current = getPlayerMoney(uuid);

        if (current >= amount) {
            setPlayerMoney(uuid, current - amount);
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static String formatMoney(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator('`');

        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);

        return format.format(amount);
    }
}
package de.niclasl.teamWar.money;

import de.niclasl.teamWar.money.manager.MoneyManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class TeamWarEconomy implements Economy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "TeamWarEconomy";
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        String teamName = TeamManager.getTeamOfPlayer(player.getUniqueId());
        if (teamName == null) return 0.0;
        return MoneyManager.getTeamMoney(teamName);
    }

    @Override
    public double getBalance(String s, String s1) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return 0;
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        // Du benutzt eh schon deinen Code, also nur Success zurückgeben
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        // Du benutzt eh schon deinen Code, also nur Success zurückgeben
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    @Override
    public double getBalance(String s) {
        return 0;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    // Bank-Funktionen stubben
    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }
    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    @Override
    public String currencyNamePlural() {
        return "Coins";
    }
    @Override
    public String currencyNameSingular() {
        return "Coin";
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public String format(double amount) {
        return amount + " Coins";
    }
    @Override
    public int fractionalDigits() {
        return 0;
    }
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }
}

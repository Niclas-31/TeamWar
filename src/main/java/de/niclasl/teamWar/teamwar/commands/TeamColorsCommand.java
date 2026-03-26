package de.niclasl.teamWar.teamwar.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TeamColorsCommand implements CommandExecutor, TabCompleter {

    private final ChatColor[] colors = {
            ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GRAY,
            ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.BLACK, ChatColor.WHITE, ChatColor.DARK_GRAY,
            ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA,
            ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD
    };

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("For players only.");
            return true;
        }

        int page = 1;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number (1-3).");
                return true;
            }
        }

        showPage(player, page);
        return true;
    }

    private void showPage(Player player, int page) {
        int[] sizes = {5, 5, 6};
        int startIndex = 0;
        for (int i = 0; i < page - 1; i++) {
            startIndex += sizes[i];
        }
        int endIndex = Math.min(startIndex + sizes[page - 1], colors.length);

        player.sendMessage(ChatColor.BOLD + "Team Colors - Page " + page + "/3");
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(colors[i] + colors[i].getName());
        }

        TextComponent nav = new TextComponent("");

        if (page > 1) {
            TextComponent prev = new TextComponent(ChatColor.GOLD + "[Previous Page] ");
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team-colors " + (page - 1)));
            nav.addExtra(prev);
        }
        if (page < 3) {
            TextComponent next = new TextComponent(ChatColor.GOLD + "[Next Page]");
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team-colors " + (page + 1)));
            nav.addExtra(next);
        }

        player.spigot().sendMessage(nav);
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        return List.of();
    }
}

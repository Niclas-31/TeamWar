package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.TeamWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ForbiddenItems implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkInventory(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(TeamWar.getInstance(), () -> checkInventory(player), 1L);
        }
    }

    private void checkInventory(Player player) {
        if (!player.isOp()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null) continue;
                if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasEnchants()) {
                    item.getEnchantments().forEach((ench, level) -> {
                        if (level > ench.getMaxLevel()) {
                            item.removeEnchantment(ench);
                            item.addEnchantment(ench, ench.getMaxLevel());
                            player.sendMessage("§cInvalid enchantment removed: " + ench.getKeyOrThrow().getKey());
                        }
                    });
                }
            }
        }
    }
}

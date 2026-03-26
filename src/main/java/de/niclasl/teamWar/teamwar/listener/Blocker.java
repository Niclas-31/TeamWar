package de.niclasl.teamWar.teamwar.listener;

import org.bukkit.Material;
import org.bukkit.entity.Evoker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Blocker implements Listener {

    @EventHandler
    public void onEvokerDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Evoker)) return;

        event.getDrops().removeIf(item ->
                item.getType() == Material.TOTEM_OF_UNDYING
        );
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        Material type = event.getBlock().getType();

        if (type == Material.RESPAWN_ANCHOR) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cRespawn Anchors are disabled!");
        }
    }

    @EventHandler
    public void onCrystalPlace(PlayerInteractEvent event) {

        if (event.getItem() == null) return;

        if (event.getItem().getType() == Material.END_CRYSTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cEnd Crystals are disabled!");
        }
    }

    @EventHandler
    public void onMinecartPlace(PlayerInteractEvent event) {

        if (event.getItem() == null) return;

        if (event.getItem().getType() == Material.TNT_MINECART) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cTNT Minecarts are disabled!");
        }
    }
}
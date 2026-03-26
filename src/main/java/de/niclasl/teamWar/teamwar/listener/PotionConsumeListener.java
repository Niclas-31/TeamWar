package de.niclasl.teamWar.teamwar.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class PotionConsumeListener implements Listener {

    private static final Set<PotionEffectType> BLOCKED_EFFECTS = Set.of(
            PotionEffectType.INVISIBILITY,
            PotionEffectType.INSTANT_HEALTH,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.STRENGTH,
            PotionEffectType.RESISTANCE,
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.HASTE
    );

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return;

        Player player = event.getPlayer();

        for (PotionEffect effect : meta.getCustomEffects()) {
            if (BLOCKED_EFFECTS.contains(effect.getType())) {
                event.setCancelled(true);
                player.sendMessage("§cThis potion is blocked!");
                return;
            }
        }

        if (meta.getBasePotionType() != null) {
            for (PotionEffect baseEffect : meta.getBasePotionType().getPotionEffects()) {
                if (BLOCKED_EFFECTS.contains(baseEffect.getType())) {
                    event.setCancelled(true);
                    player.sendMessage("§cThis potion is blocked!");
                    return;
                }
            }
        }
    }
}
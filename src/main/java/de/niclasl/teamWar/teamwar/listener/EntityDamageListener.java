package de.niclasl.teamWar.teamwar.listener;

import de.niclasl.teamWar.teamwar.manager.PvPManager;
import de.niclasl.teamWar.teamwar.manager.TeamManager;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null) return;

        String teamVictim = TeamManager.getTeamOfPlayer(victim.getUniqueId());
        String teamAttacker = TeamManager.getTeamOfPlayer(attacker.getUniqueId());

        if (teamVictim != null && teamVictim.equalsIgnoreCase(teamAttacker)) {
            event.setCancelled(true);
            return;
        }

        if (!PvPManager.isPvpAllowed()) {
            event.setCancelled(true);
            attacker.sendMessage("§cPvP is disabled for the first " + PvPManager.getMaxWeeks() + " weeks!");
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            event.setDamage(Math.min(event.getDamage(), 12.0));
        }

        if (event.getDamager() instanceof Firework) {
            event.setDamage(Math.min(event.getDamage(), 6.0));
        }

        if (attacker.hasPotionEffect(PotionEffectType.STRENGTH)) {
            PotionEffect effect = attacker.getPotionEffect(PotionEffectType.STRENGTH);
            if (effect != null && effect.getAmplifier() > 0) {
                event.setDamage(event.getDamage() * 0.75);
            }
        }
    }
}

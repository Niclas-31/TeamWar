package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.pvp.PvPManager;
import de.niclasl.teamwar.team.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class EntityDamage {

    @SubscribeEvent
    public static void onEntityDamageByEntity(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;

        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        ServerPlayer attacker = null;

        if (sourceEntity instanceof ServerPlayer player) {
            attacker = player;
        } else if (directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) {
            attacker = player;
        }

        if (attacker == null) return;

        TeamManager teamManager = TeamWar.getTeamManager();

        String teamVictim = teamManager.getTeamOfPlayer(victim.getUUID());
        String teamAttacker = teamManager.getTeamOfPlayer(attacker.getUUID());

        if (teamVictim != null && teamVictim.equalsIgnoreCase(teamAttacker)) {
            event.setNewDamage(0.0F);
            return;
        }

        PvPManager pvpManager = TeamWar.getPvPManager();

        if (!pvpManager.isPvpAllowed()) {
            event.setNewDamage(0.0F);

            attacker.sendSystemMessage(Component.translatable(
                    "message.game.pvp_disabled", pvpManager.getMaxWeeks())
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        float damage = event.getNewDamage();

        if (event.getSource().is(DamageTypes.EXPLOSION) ||
                event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) {
            damage = (float) Math.min(damage, 12.0);
        }

        if (directEntity instanceof FireworkRocketEntity) {
            damage = (float) Math.min(damage, 6.0);
        }

        MobEffectInstance strength = attacker.getEffect(MobEffects.STRENGTH);

        if (strength != null && strength.getAmplifier() > 0) {
            damage *= 0.75F;
        }

        event.setNewDamage(damage);
    }
}
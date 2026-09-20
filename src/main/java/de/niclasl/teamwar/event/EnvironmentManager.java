package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class EnvironmentManager {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void startEnvironmentTask(ServerTickEvent.Post event) {
        tickCounter++;

        if (tickCounter < 100) {
            return;
        }

        tickCounter = 0;

        MinecraftServer server = event.getServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyEnvironmentEffects(player);
        }
    }

    @SubscribeEvent
    public static void applyRainArrowEffect(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getDirectEntity() instanceof Arrow arrow)) {
            return;
        }

        if (!(arrow.level() instanceof ServerLevel level)) {
            return;
        }

        if (!level.isRaining()) {
            return;
        }

        double spread = level.isThundering() ? 0.2 : 0.1;

        Vec3 velocity = arrow.getDeltaMovement();

        velocity = velocity.add(
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread
        );

        arrow.setDeltaMovement(velocity);

        float modifier = level.isThundering()
                ? 0.9F
                : 0.8F;

        arrow.setBaseDamage(event.getNewDamage() * modifier);

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,
                arrow.getX(),
                arrow.getY(),
                arrow.getZ(),
                5,
                0.1,
                0.1,
                0.1,
                0.01
        );
    }

    private static void applyEnvironmentEffects(ServerPlayer player) {
        ServerLevel level = player.level();

        clearEnvironmentEffects(player);

        if (level.isThundering()) {
            applyThunderEffects(player);
        } else if (level.isRaining()) {
            applyRainEffects(player);
        } else {
            applyClearWeatherEffects(player);
        }

        applyBiomeEffects(player);
    }

    private static void applyBiomeEffects(ServerPlayer player) {
        ServerLevel level = player.level();

        BlockPos pos = player.blockPosition();

        var biome = level.getBiome(pos);

        if (biome == Biomes.SNOWY_PLAINS ||
                biome == Biomes.ICE_SPIKES ||
                biome == Biomes.FROZEN_PEAKS ||
                biome == Biomes.GROVE) {
            applySnowEffects(player);
        }
        else if (biome == Biomes.DESERT ||
                biome == Biomes.BADLANDS ||
                biome == Biomes.SAVANNA) {
            applyDesertEffects(player);
        }
        else if (biome == Biomes.JUNGLE ||
                biome == Biomes.BAMBOO_JUNGLE ||
                biome == Biomes.SPARSE_JUNGLE) {
            applyJungleEffects(player);
        }
    }

    private static void clearEnvironmentEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.HUNGER);
        player.removeEffect(MobEffects.JUMP_BOOST);
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.STRENGTH);
    }

    private static void applyClearWeatherEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.SPEED,
                120,
                0,
                true,
                false
                )
        );
    }

    private static void applyRainEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                120,
                0,
                true,
                false
                )
        );
    }

    private static void applyThunderEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH,
                120,
                0,
                true,
                false
                )
        );
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                120,
                0,
                true,
                false
                )
        );
    }

    private static void applySnowEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                120,
                0,
                true,
                false
                )
        );
    }

    private static void applyDesertEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.HUNGER,
                120,
                0,
                true,
                false
                )
        );
    }

    private static void applyJungleEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.JUMP_BOOST,
                120,
                0,
                true,
                false
                )
        );

        if (Math.random() < 0.05) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    100,
                    0,
                    true,
                    false
                    )
            );
        }
    }
}
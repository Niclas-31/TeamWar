package de.niclasl.teamwar.registries.entities.boss.goal;

import de.niclasl.teamwar.registries.entities.boss.ShadowKnightEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.Set;

public class ShadowKnightTeleportGoal extends Goal {

  private static final double TRIGGER_DISTANCE = 8.0;
  private static final double MELEE_RANGE = 3.5;
  private static final double TELEPORT_DISTANCE = 2.5;

  private static final int TELEPORT_ANIMATION_TICKS = 6;
  private static final int TELEPORT_COOLDOWN_TICKS = 100;

  private final ShadowKnightEntity boss;

  private int teleportTicks;
  private int nextTeleportTick;

  public ShadowKnightTeleportGoal(ShadowKnightEntity boss) {
    this.boss = boss;
    this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    LivingEntity target = boss.getTarget();

    if (target == null || !target.isAlive()) {
      return false;
    }

    double distanceSqr = boss.distanceToSqr(target);

    return distanceSqr > MELEE_RANGE * MELEE_RANGE && distanceSqr <= TRIGGER_DISTANCE * TRIGGER_DISTANCE && boss.tickCount >= nextTeleportTick;
  }

  @Override
  public boolean canContinueToUse() {
    return teleportTicks > 0;
  }

  @Override
  public void start() {
    teleportTicks = TELEPORT_ANIMATION_TICKS;

    boss.getNavigation().stop();

    boss.startTeleportAnimation();
  }

  @Override
  public void tick() {
    LivingEntity target = boss.getTarget();

    if (target == null || !target.isAlive()) {
      teleportTicks = 0;
      return;
    }

    boss.getNavigation().stop();
    boss.getLookControl().setLookAt(target);

    teleportTicks--;

    if (teleportTicks <= 0) {
      if (boss.level() instanceof ServerLevel serverLevel) {
        teleportBehindTarget(serverLevel, target);
      }

      nextTeleportTick = boss.tickCount + TELEPORT_COOLDOWN_TICKS;
    }
  }

  private void teleportBehindTarget(ServerLevel serverLevel, LivingEntity target) {
    double lookX = -Math.sin(Math.toRadians(target.getYRot()));
    double lookZ = Math.cos(Math.toRadians(target.getYRot()));

    double targetX = target.getX() - lookX * TELEPORT_DISTANCE;
    double targetY = target.getY();
    double targetZ = target.getZ() - lookZ * TELEPORT_DISTANCE;

    BlockPos destination = BlockPos.containing(targetX, targetY, targetZ);

    if (!serverLevel.getBlockState(destination.below()).isFaceSturdy(serverLevel, destination.below(), Direction.UP)) {
      return;
    }

    AABB destinationBox = boss.getBoundingBox().move(
            targetX - boss.getX(),
            targetY - boss.getY(),
            targetZ - boss.getZ()
    );

    if (!serverLevel.noCollision(boss, destinationBox)) {
      return;
    }

    float yaw = (float) (Math.toDegrees(Math.atan2(target.getZ() - targetZ, target.getX() - targetX)) - 90.0);

    boss.teleportTo(serverLevel, targetX, targetY, targetZ, Set.of(), yaw, boss.getXRot(), false);
  }

  @Override
  public void stop() {
    teleportTicks = 0;
    boss.getNavigation().stop();
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }
}
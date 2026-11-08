package de.niclasl.teamwar.registries.entities.boss.goal;

import de.niclasl.teamwar.registries.entities.boss.ShadowKnightEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ShadowKnightMeleeAttackGoal extends Goal {
  private static final int PREPARATION_TICKS = 10;
  private static final int SWING_TICKS = 6;
  private static final int RECOVERY_TICKS = 10;

  private static final double ATTACK_RANGE = 3.5;
  private static final float ATTACK_DAMAGE = 24.0F;

  private final ShadowKnightEntity boss;

  private int attackTicks;
  private int recoveryTicks;
  private boolean attacking;
  private float swingStartYaw;
  private boolean swingAnimationStarted;

  private final Set<UUID> hitPlayers = new HashSet<>();

  public ShadowKnightMeleeAttackGoal(ShadowKnightEntity boss) {
    this.boss = boss;
    this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    LivingEntity target = boss.getTarget();

    return target != null
            && target.isAlive()
            && boss.distanceToSqr(target)
            <= ATTACK_RANGE * ATTACK_RANGE;
  }

  @Override
  public boolean canContinueToUse() {
    LivingEntity target = boss.getTarget();

    return attacking
            || recoveryTicks > 0
            || (target != null && target.isAlive()
            && boss.distanceToSqr(target)
            <= ATTACK_RANGE * ATTACK_RANGE);
  }

  @Override
  public void start() {
    attackTicks = 0;
    recoveryTicks = 0;
    attacking = false;
    hitPlayers.clear();
  }

  @Override
  public void tick() {
    LivingEntity target = boss.getTarget();

    if (target == null || !target.isAlive()) {
      return;
    }

    boss.getNavigation().stop();
    boss.getLookControl().setLookAt(target);

    if (recoveryTicks > 0) {
      recoveryTicks--;
      return;
    }

    if (!attacking) {
      attacking = true;
      attackTicks = 0;
      hitPlayers.clear();
      swingStartYaw = boss.getYRot();
      swingAnimationStarted = false;
    }

    attackTicks++;

    if (attackTicks <= PREPARATION_TICKS) {
      return;
    }

    if (attackTicks <= PREPARATION_TICKS + SWING_TICKS) {
      if (!swingAnimationStarted) {
        boss.startAttackAnimation();
        swingAnimationStarted = true;
      }

      checkSwordCollision();
      return;
    }

    attacking = false;
    recoveryTicks = RECOVERY_TICKS;
  }

  private void checkSwordCollision() {
    if (!(boss.level() instanceof ServerLevel serverLevel)) {
      return;
    }

    double previousProgress = Math.max(
            0.0,
            (attackTicks - 1 - PREPARATION_TICKS)
                    / (double) SWING_TICKS
    );

    double currentProgress = Math.min(
            1.0,
            (attackTicks - PREPARATION_TICKS)
                    / (double) SWING_TICKS
    );

    double previousAngle = -90.0 + previousProgress * 180.0;
    double currentAngle = -90.0 + currentProgress * 180.0;

    int angleSteps = Math.max(
            1,
            (int) Math.ceil(
                    Math.abs(currentAngle - previousAngle) / 5.0
            )
    );

    List<Player> players = boss.level().getEntitiesOfClass(
            Player.class,
            boss.getBoundingBox().inflate(
                    ATTACK_RANGE + 1.0,
                    1.5,
                    ATTACK_RANGE + 1.0
            )
    );

    for (Player player : players) {
      if (!player.isAlive()
              || player.isSpectator()
              || hitPlayers.contains(player.getUUID())) {
        continue;
      }

      var playerBox = player.getBoundingBox().inflate(0.2);

      boolean hit = false;

      for (int angleStep = 0;
           angleStep <= angleSteps && !hit;
           angleStep++) {

        double fraction = angleStep / (double) angleSteps;

        double angle = Math.toRadians(
                swingStartYaw
                        + previousAngle
                        + (currentAngle - previousAngle) * fraction
        );

        double directionX = -Math.sin(angle);
        double directionZ = Math.cos(angle);

        for (double distance = 0.7;
             distance <= ATTACK_RANGE;
             distance += 0.2) {

          Vec3 bladePoint = new Vec3(
                  boss.getX() + directionX * distance,
                  boss.getY() + 1.0,
                  boss.getZ() + directionZ * distance
          );

          if (playerBox.contains(bladePoint)) {
            hit = true;
            break;
          }
        }
      }

      if (hit && hitPlayers.add(player.getUUID())) {
        player.hurtServer(
                serverLevel,
                boss.damageSources().mobAttack(boss),
                ATTACK_DAMAGE
        );
      }
    }
  }

  @Override
  public void stop() {
    attacking = false;
    attackTicks = 0;
    recoveryTicks = 0;
    hitPlayers.clear();
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }
}
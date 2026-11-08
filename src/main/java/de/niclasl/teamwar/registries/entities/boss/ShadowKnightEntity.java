package de.niclasl.teamwar.registries.entities.boss;

import de.niclasl.teamwar.registries.entities.boss.goal.ShadowKnightMeleeAttackGoal;
import de.niclasl.teamwar.registries.entities.boss.goal.ShadowKnightTeleportGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class ShadowKnightEntity extends AbstractTeamWarBoss {

  public final AnimationState idleAnimationState = new AnimationState();
  public final AnimationState walkAnimationState = new AnimationState();
  public final AnimationState attackAnimationState = new AnimationState();
  public final AnimationState jumpAttackAnimationState = new AnimationState();
  public final AnimationState teleportAnimationState = new AnimationState();
  public final AnimationState deathAnimationState = new AnimationState();

  private static final double JUMP_ATTACK_MIN_RANGE = 5.0;
  private static final double JUMP_ATTACK_MAX_RANGE = 12.0;
  private static final double JUMP_ATTACK_RADIUS = 3.0;
  private static final float JUMP_ATTACK_DAMAGE = 24.0F;

  private static final int JUMP_PREPARATION_TICKS = 20;
  private static final int JUMP_ATTACK_COOLDOWN = 100;

  private int jumpPreparationTicks;
  private int jumpAttackCooldown;
  private boolean jumpAttackInProgress;
  private boolean hasLeftGround;
  private Vec3 jumpTargetPosition;

  public ShadowKnightEntity(EntityType<? extends Monster> type, Level level) {
    super(type, level);
  }

  @Override
  protected Goal createMeleeAttackGoal() {
    return new ShadowKnightMeleeAttackGoal(this);
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();

    this.goalSelector.addGoal(3, new ShadowKnightTeleportGoal(this));
  }

  @Override
  public void die(@NonNull DamageSource source) {
    super.die(source);

    this.deathAnimationState.start(this.tickCount);
  }

  @Override
  public void tick() {
    super.tick();

    if (!this.level().isClientSide()) {
      if (this.jumpAttackCooldown > 0) {
        this.jumpAttackCooldown--;
      }

      if (this.jumpAttackInProgress) {
        tickJumpAttack();
      } else {
        tryStartJumpAttack();
      }
    }

    updateAnimationStates();
  }

  private void tryStartJumpAttack() {
    if (this.jumpAttackCooldown > 0) {
      return;
    }

    if (this.getTarget() == null || !this.getTarget().isAlive()) {
      return;
    }

    double distanceSqr = this.distanceToSqr(this.getTarget());

    if (distanceSqr < JUMP_ATTACK_MIN_RANGE * JUMP_ATTACK_MIN_RANGE
            || distanceSqr > JUMP_ATTACK_MAX_RANGE * JUMP_ATTACK_MAX_RANGE) {
      return;
    }

    this.jumpTargetPosition = this.getTarget().position();

    this.jumpAttackInProgress = true;
    this.jumpPreparationTicks = JUMP_PREPARATION_TICKS;
    this.hasLeftGround = false;

    this.getNavigation().stop();
  }

  public void startAttackAnimation() {
    this.attackAnimationState.start(this.tickCount);
  }

  public void startTeleportAnimation() {
    if (!(this.level() instanceof ServerLevel serverLevel)) {
      return;
    }

    this.teleportAnimationState.start(this.tickCount);

    serverLevel.broadcastEntityEvent(this, (byte) 60);
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == 60) {
      this.teleportAnimationState.start(this.tickCount);
      return;
    }

    super.handleEntityEvent(id);
  }

  private void tickJumpAttack() {
    this.getNavigation().stop();

    if (this.jumpPreparationTicks > 0) {
      this.jumpPreparationTicks--;

      if (this.jumpPreparationTicks == 0) {
        performJump();
      }

      return;
    }

    if (!this.onGround()) {
      this.hasLeftGround = true;
    }

    if (this.hasLeftGround && this.onGround()) {
      performLandingAttack();
    }
  }

  private void performJump() {
    if (this.jumpTargetPosition == null) {
      finishJumpAttack();
      return;
    }

    Vec3 direction = this.jumpTargetPosition.subtract(this.position());

    Vec3 horizontalDirection = new Vec3(direction.x, 0.0, direction.z).normalize();

    double horizontalSpeed = 0.75;
    double verticalSpeed = 0.65;

    this.setDeltaMovement(
            horizontalDirection.x * horizontalSpeed,
            verticalSpeed,
            horizontalDirection.z * horizontalSpeed
    );
  }

  private void performLandingAttack() {
    for (Player player : this.level().getEntitiesOfClass(
            Player.class,
            this.getBoundingBox().inflate(JUMP_ATTACK_RADIUS),
            Player::isAlive
    )) {
      if (player.distanceToSqr(this.position()) <= JUMP_ATTACK_RADIUS * JUMP_ATTACK_RADIUS) {

        if (this.level() instanceof ServerLevel serverLevel) {
          player.hurtServer(
                  serverLevel,
                  this.damageSources().mobAttack(this),
                  JUMP_ATTACK_DAMAGE
          );
        }
      }
    }

    this.jumpAttackCooldown = JUMP_ATTACK_COOLDOWN;
    finishJumpAttack();
  }

  private void finishJumpAttack() {
    this.jumpAttackInProgress = false;
    this.hasLeftGround = false;
    this.jumpPreparationTicks = 0;
    this.jumpTargetPosition = null;
  }

  private void updateAnimationStates() {
    if (this.jumpAttackInProgress) {
      this.idleAnimationState.stop();
      this.walkAnimationState.stop();
      this.jumpAttackAnimationState.startIfStopped(this.tickCount);
      return;
    }

    this.jumpAttackAnimationState.stop();

    boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && this.onGround();

    if (moving) {
      this.idleAnimationState.stop();
      this.walkAnimationState.startIfStopped(this.tickCount);
    } else {
      this.walkAnimationState.stop();
      this.idleAnimationState.startIfStopped(this.tickCount);
    }
  }
}
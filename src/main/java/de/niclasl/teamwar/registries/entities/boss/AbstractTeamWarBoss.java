package de.niclasl.teamwar.registries.entities.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AbstractTeamWarBoss extends Monster {

  protected AbstractTeamWarBoss(EntityType<? extends Monster> type, Level level) {
    super(type, level);
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();

    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(2, createMeleeAttackGoal());
    this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
    this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
    this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
  }

  protected Goal createMeleeAttackGoal() {
    return new MeleeAttackGoal(this, 1.0D, false);
  }
}
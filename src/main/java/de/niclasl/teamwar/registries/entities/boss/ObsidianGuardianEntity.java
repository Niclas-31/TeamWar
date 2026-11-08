package de.niclasl.teamwar.registries.entities.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class ObsidianGuardianEntity extends AbstractTeamWarBoss {

  public ObsidianGuardianEntity(EntityType<? extends Monster> type, Level level) {
    super(type, level);
  }
}
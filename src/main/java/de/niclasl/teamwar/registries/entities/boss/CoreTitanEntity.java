package de.niclasl.teamwar.registries.entities.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class CoreTitanEntity extends AbstractTeamWarBoss {

  public CoreTitanEntity(EntityType<? extends Monster> type, Level level) {
    super(type, level);
  }
}
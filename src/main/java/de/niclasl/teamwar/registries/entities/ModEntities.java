package de.niclasl.teamwar.registries.entities;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.registries.entities.boss.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
          DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TeamWar.MOD_ID);

  public static ResourceKey<EntityType<?>> SHADOW_KNIGHT_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "shadow_knight")
  );
  public static ResourceKey<EntityType<?>> OBSIDIAN_GUARDIAN_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "obsidian_guardian")
  );
  public static ResourceKey<EntityType<?>> VOID_WRAITH_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "void_wraith")
  );
  public static ResourceKey<EntityType<?>> CORE_TITAN_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "core_titan")
  );
  public static ResourceKey<EntityType<?>> NIGHT_STALKER_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "night_stalker")
  );
  public static ResourceKey<EntityType<?>> VOID_HEART_KEY = ResourceKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath(TeamWar.MOD_ID, "void_heart")
  );

  public static final Supplier<EntityType<ShadowKnightEntity>> SHADOW_KNIGHT = ENTITY_TYPES.register(
          "shadow_knight",
          () -> EntityType.Builder.of(ShadowKnightEntity::new, MobCategory.MONSTER)
                  .sized(0.8F, 2.2F).build(SHADOW_KNIGHT_KEY)
  );

  public static final Supplier<EntityType<ObsidianGuardianEntity>> OBSIDIAN_GUARDIAN = ENTITY_TYPES.register(
          "obsidian_guardian",
          () -> EntityType.Builder.of(ObsidianGuardianEntity::new, MobCategory.MONSTER)
                  .sized(1.4F, 2.8F).build(OBSIDIAN_GUARDIAN_KEY)
  );

  public static final Supplier<EntityType<VoidWraithEntity>> VOID_WRAITH = ENTITY_TYPES.register(
          "void_wraith",
          () -> EntityType.Builder.of(VoidWraithEntity::new, MobCategory.MONSTER)
                  .sized(0.9F, 2.0F).build(VOID_WRAITH_KEY)
  );

  public static final Supplier<EntityType<CoreTitanEntity>> CORE_TITAN = ENTITY_TYPES.register(
          "core_titan",
          () -> EntityType.Builder.of(CoreTitanEntity::new, MobCategory.MONSTER)
                  .sized(2.0F, 3.5F).build(CORE_TITAN_KEY)
  );

  public static final Supplier<EntityType<NightStalkerEntity>> NIGHT_STALKER = ENTITY_TYPES.register(
          "night_stalker",
          () -> EntityType.Builder.of(NightStalkerEntity::new, MobCategory.MONSTER)
                  .sized(0.9F, 2.1F).build(NIGHT_STALKER_KEY)
  );

  public static final Supplier<EntityType<VoidHeartEntity>> VOID_HEART = ENTITY_TYPES.register(
          "void_heart",
          () -> EntityType.Builder.of(VoidHeartEntity::new, MobCategory.MONSTER)
                  .sized(1.5F, 2.5F).build(VOID_HEART_KEY)
  );

  public static void register(IEventBus eventBus) {
    ENTITY_TYPES.register(eventBus);
    eventBus.addListener(ModEntities::registerAttributes);
  }

  private static void registerAttributes(EntityAttributeCreationEvent event) {
    event.put(SHADOW_KNIGHT.get(), attributes(
            120.0, 0.32, 10.0, 8.0, 40.0
    ));

    event.put(OBSIDIAN_GUARDIAN.get(), attributes(
            250.0, 0.20, 14.0, 18.0, 32.0
    ));

    event.put(VOID_WRAITH.get(), attributes(
            100.0, 0.30, 8.0, 4.0, 48.0
    ));

    event.put(CORE_TITAN.get(), attributes(
            400.0, 0.16, 18.0, 16.0, 32.0
    ));

    event.put(NIGHT_STALKER.get(), attributes(
            150.0, 0.38, 12.0, 6.0, 48.0
    ));

    event.put(VOID_HEART.get(), attributes(
            500.0, 0.24, 16.0, 12.0, 48.0
    ));
  }

  private static AttributeSupplier attributes(
          double health,
          double speed,
          double damage,
          double armor,
          double followRange
  ) {
    return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, health)
            .add(Attributes.MOVEMENT_SPEED, speed)
            .add(Attributes.ATTACK_DAMAGE, damage)
            .add(Attributes.ARMOR, armor)
            .add(Attributes.FOLLOW_RANGE, followRange)
            .build();
  }
}
package de.niclasl.teamwar.datagen;

import de.niclasl.teamwar.TeamWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TeamWar.MOD_ID)
public class Datagen {

  @SubscribeEvent
  public static void gatherClientData(GatherDataEvent.Client event) {
    event.createProvider(ModBlockTagProvider::new);
  }
}
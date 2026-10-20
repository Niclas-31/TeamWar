package de.niclasl.teamwar.event;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.repair.WorldRepairManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.io.IOException;

@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.DEDICATED_SERVER)
public class WorldRepairEvents {

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) throws IOException {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldRepairManager.tick(level);
        }
    }
}
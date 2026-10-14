package de.niclasl.teamwar.client;

import de.niclasl.teamwar.TeamWar;
import de.niclasl.teamwar.client.screen.UpgradeScreen;
import de.niclasl.teamwar.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = TeamWar.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TeamWar.MOD_ID, value = Dist.CLIENT)
public class TeamWarClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.UPGRADE_MENU.get(), UpgradeScreen::new);
    }
}
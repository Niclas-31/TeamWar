package de.niclasl.teamwar;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(TeamWar.MOD_ID)
public class TeamWar {
    public static final String MOD_ID = "teamwar";

    private static final Logger LOGGER = LogUtils.getLogger();

    public TeamWar(IEventBus modEventBus, ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}

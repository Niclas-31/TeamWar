package de.niclasl.teamwar.worldgen.density;

import com.mojang.serialization.MapCodec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IslandDensityFunction {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> REGISTER =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TeamWar.MOD_ID);

    public static void init() {
        REGISTER.register("islandcont", () -> IslandContinentalNoiseFunction.CODEC);
    }
}
package de.niclasl.teamwar.worldgen.biome_source;

import com.mojang.serialization.MapCodec;
import de.niclasl.teamwar.TeamWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBiomeSource {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> REGISTER =
            DeferredRegister.create(Registries.BIOME_SOURCE, TeamWar.MOD_ID);

    public static void init() {
        REGISTER.register("island", () -> ModIslandBiomeSource.CODEC);
    }
}
package de.niclasl.teamwar.worldgen.biome_source;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public class ModIslandBiomeSource extends BiomeSource {

    public static final MapCodec<ModIslandBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.CODEC.fieldOf("ocean")
                            .forGetter(source -> source.ocean),
                    Biome.CODEC.fieldOf("dark_forest")
                            .forGetter(source -> source.darkForest),
                    Biome.CODEC.fieldOf("snowy_taiga")
                            .forGetter(source -> source.snowyTaiga),
                    Biome.CODEC.fieldOf("desert")
                            .forGetter(source -> source.desert),
                    Biome.CODEC.fieldOf("jungle")
                            .forGetter(source -> source.jungle),
                    Biome.CODEC.fieldOf("plains")
                            .forGetter(source -> source.plains)
            ).apply(instance, ModIslandBiomeSource::new));

    private static final double ISLAND_DISTANCE = 500.0;

    private static final double LARGE_ISLAND_RADIUS = 270.0;
    private static final double SMALL_ISLAND_RADIUS = 140.0;

    private final Holder<Biome> ocean;
    private final Holder<Biome> darkForest;
    private final Holder<Biome> snowyTaiga;
    private final Holder<Biome> desert;
    private final Holder<Biome> jungle;
    private final Holder<Biome> plains;

    public ModIslandBiomeSource(Holder<Biome> ocean, Holder<Biome> darkForest, Holder<Biome> snowyTaiga,
            Holder<Biome> desert, Holder<Biome> jungle, Holder<Biome> plains) {
        this.ocean = ocean;
        this.darkForest = darkForest;
        this.snowyTaiga = snowyTaiga;
        this.desert = desert;
        this.jungle = jungle;
        this.plains = plains;
    }

    @Override
    protected @NonNull MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(
                ocean,
                darkForest,
                snowyTaiga,
                desert,
                jungle,
                plains
        );
    }

    @Override
    public @NonNull Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.@NonNull Sampler sampler) {
        int blockX = x << 2;
        int blockZ = z << 2;

        if (isInside(
                blockX, blockZ,
                0, 0,
                LARGE_ISLAND_RADIUS
        )) {
            return darkForest;
        }

        if (isInside(
                blockX, blockZ,
                -ISLAND_DISTANCE, -ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        )) {
            return snowyTaiga;
        }

        if (isInside(
                blockX, blockZ,
                ISLAND_DISTANCE, -ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        )) {
            return desert;
        }

        if (isInside(
                blockX, blockZ,
                -ISLAND_DISTANCE, ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        )) {
            return jungle;
        }

        if (isInside(
                blockX, blockZ,
                ISLAND_DISTANCE, ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        )) {
            return plains;
        }

        return ocean;
    }

    private static boolean isInside(int x, int z, double centerX, double centerZ, double radius) {
        double dx = x - centerX;
        double dz = z - centerZ;

        return dx * dx + dz * dz <= radius * radius;
    }
}
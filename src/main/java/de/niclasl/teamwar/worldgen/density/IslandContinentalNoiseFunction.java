package de.niclasl.teamwar.worldgen.density;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

public class IslandContinentalNoiseFunction implements DensityFunction {

    public static final MapCodec<IslandContinentalNoiseFunction> CODEC =
            MapCodec.unit(IslandContinentalNoiseFunction::new);

    private static final double ISLAND_DISTANCE = 500.0;

    private static final double LARGE_ISLAND_RADIUS = 250.0;
    private static final double SMALL_ISLAND_RADIUS = 120.0;

    @Override
    public double compute(FunctionContext context) {
        return calculate(context.blockX(), context.blockZ());
    }

    private static double calculate(double x, double z) {

        double volcano = islandDensity(
                x, z,
                0, 0,
                LARGE_ISLAND_RADIUS
        );

        double ice = islandDensity(
                x, z,
                -ISLAND_DISTANCE, -ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        );

        double desert = islandDensity(
                x, z,
                ISLAND_DISTANCE, -ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        );

        double tropical = islandDensity(
                x, z,
                -ISLAND_DISTANCE, ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        );

        double plains = islandDensity(
                x, z,
                ISLAND_DISTANCE, ISLAND_DISTANCE,
                SMALL_ISLAND_RADIUS
        );

        return Math.max(
                volcano,
                Math.max(
                        Math.max(ice, desert),
                        Math.max(tropical, plains)
                )
        );
    }

    private static double islandDensity(double x, double z, double centerX, double centerZ, double radius) {
        double dx = x - centerX;
        double dz = z - centerZ;

        double distance = Math.sqrt(dx * dx + dz * dz);

        double density = 1.0 - (distance / radius);

        return Math.max(-1.0, density);
    }

    @Override
    public void fillArray(double @NotNull [] output, @NotNull ContextProvider contextProvider) {
        for (int i = 0; i < output.length; i++) {
            FunctionContext context = contextProvider.forIndex(i);
            output[i] = compute(context);
        }
    }

    @Override
    public @NotNull DensityFunction mapChildren(@NotNull Visitor visitor) {
        return this;
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KeyDispatchDataCodec.of(CODEC);
    }
}
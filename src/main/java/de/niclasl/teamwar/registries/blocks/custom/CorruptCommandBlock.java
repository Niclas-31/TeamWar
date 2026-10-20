package de.niclasl.teamwar.registries.blocks.custom;

import de.niclasl.teamwar.repair.WorldRepairManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CorruptCommandBlock extends Block {

    public CorruptCommandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroy(@NonNull Level level, @NonNull Player player, @NonNull BlockPos pos,
                              @NonNull BlockState state, @Nullable BlockEntity blockEntity,
                              @NonNull ItemStack destroyedWith
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);

        if (level instanceof ServerLevel serverLevel) {
            WorldRepairManager.start(serverLevel);
        }
    }
}
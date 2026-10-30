package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4all.sit.Sit;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LogBenchBlock extends AxeStoringSeatBlock {

    public static final MapCodec<LogBenchBlock> CODEC = BlockBehaviour.simpleCodec(LogBenchBlock::new);

    private static final VoxelShape NORTH_SOUTH = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);
    private static final VoxelShape EAST_WEST = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);

    public LogBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends LogBenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape shapeFor(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }

    @Override
    protected double sitHeight() {
        return Sit.BENCH_HEIGHT;
    }
}

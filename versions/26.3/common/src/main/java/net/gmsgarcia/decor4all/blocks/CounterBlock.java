package net.gmsgarcia.decor4all.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class CounterBlock extends CounterBlockBase {

    public static final BooleanProperty FRONT = BooleanProperty.create("front");

    public CounterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRONT);
    }

    @Override
    protected BlockState applyFront(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
        return state.setValue(FRONT, isCounter(level.getBlockState(pos.relative(facing))));
    }

    @Override
    protected boolean joinsInFront(BlockState state) {
        return state.getValue(FRONT);
    }
}

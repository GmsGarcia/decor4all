package net.gmsgarcia.decor4all.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class CounterBlock extends CounterBlockBase {

    public static final BooleanProperty FRONT = BooleanProperty.create("front");
    public static final EnumProperty<CounterCorner> CORNER =
            EnumProperty.create("corner", CounterCorner.class);

    public CounterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRONT, CORNER);
    }

    @Override
    protected BlockState applyFront(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
        boolean left = state.getValue(LEFT);
        boolean right = state.getValue(RIGHT);
        boolean front = isCounter(level.getBlockState(pos.relative(facing)));
        boolean leftFill = left || alignsWithNeighbourBack(level, pos, facing, facing.getClockWise());
        boolean rightFill = right || alignsWithNeighbourBack(level, pos, facing, facing.getCounterClockWise());
        CounterCorner corner = CounterCorner.NONE;
        if (front) {
            if (left && right) {
                corner = CounterCorner.BOTH;
            } else if (left) {
                corner = CounterCorner.LEFT;
            } else if (right) {
                corner = CounterCorner.RIGHT;
            }
        }
        return state.setValue(FRONT, front)
                .setValue(CORNER, corner)
                .setValue(LEFT, leftFill)
                .setValue(RIGHT, rightFill);
    }

    @Override
    protected CounterCorner cornerKind(BlockState state) {
        return state.getValue(CORNER);
    }

    @Override
    protected boolean joinsInFront(BlockState state) {
        return state.getValue(FRONT);
    }

    private static boolean alignsWithNeighbourBack(LevelReader level, BlockPos pos, Direction facing,
                                                   Direction side) {
        for (Direction neighbour : new Direction[] {facing, facing.getOpposite()}) {
            BlockState state = level.getBlockState(pos.relative(neighbour));
            if (isCounter(state) && state.getValue(FACING).getOpposite() == side) {
                return true;
            }
        }
        return false;
    }
}

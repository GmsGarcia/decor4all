package net.gmsgarcia.decor4all.blocks;

import net.gmsgarcia.decor4all.sit.Sit;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HighBenchBlock extends SeatBlock {
    private static final VoxelShape TOP_NS = Block.box(0.0D, 6.0D, 3.0D, 16.0D, 8.0D, 13.0D);
    private static final VoxelShape MID_NS = Block.box(0.0D, 5.0D, 4.0D, 16.0D, 6.0D, 12.0D);
    private static final VoxelShape BOTTOM_NS = Block.box(0.0D, 4.0D, 5.0D, 16.0D, 5.0D, 11.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_NS = Block.box(2.0D, 3.0D, 6.0D, 4.0D, 4.0D, 10.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_NS = Block.box(2.0D, 2.0D, 5.0D, 4.0D, 3.0D, 11.0D);
    private static final VoxelShape FIRST_LEG_WEST_NS = Block.box(2.0D, 0.0D, 5.0D, 4.0D, 2.0D, 7.0D);
    private static final VoxelShape FIRST_LEG_EAST_NS = Block.box(2.0D, 0.0D, 9.0D, 4.0D, 2.0D, 11.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_NS = Block.box(12.0D, 3.0D, 6.0D, 14.0D, 4.0D, 10.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_NS = Block.box(12.0D, 2.0D, 5.0D, 14.0D, 3.0D, 11.0D);
    private static final VoxelShape SECOND_LEG_WEST_NS = Block.box(12.0D, 0.0D, 5.0D, 14.0D, 2.0D, 7.0D);
    private static final VoxelShape SECOND_LEG_EAST_NS = Block.box(12.0D, 0.0D, 9.0D, 14.0D, 2.0D, 11.0D);

    private static final VoxelShape TOP_WE = Block.box(3.0D, 6.0D, 0.0D, 13.0D, 8.0D, 16.0D);
    private static final VoxelShape MID_WE = Block.box(4.0D, 5.0D, 0.0D, 12.0D, 6.0D, 16.0D);
    private static final VoxelShape BOTTOM_WE = Block.box(5.0D, 4.0D, 0.0D, 11.0D, 5.0D, 16.0D);

    private static final VoxelShape FIRST_LEG_SUPPORT_1_WE = Block.box(6.0D, 3.0D, 2.0D, 10.0D, 4.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_SUPPORT_2_WE = Block.box(5.0D, 2.0D, 2.0D, 11.0D, 3.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_WEST_WE = Block.box(5.0D, 0.0D, 2.0D, 7.0D, 2.0D, 4.0D);
    private static final VoxelShape FIRST_LEG_EAST_WE = Block.box(9.0D, 0.0D, 2.0D, 11.0D, 2.0D, 4.0D);

    private static final VoxelShape SECOND_LEG_SUPPORT_1_WE = Block.box(6.0D, 3.0D, 12.0D, 10.0D, 4.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_SUPPORT_2_WE = Block.box(5.0D, 2.0D, 12.0D, 11.0D, 3.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_WEST_WE = Block.box(5.0D, 0.0D, 12.0D, 7.0D, 2.0D, 14.0D);
    private static final VoxelShape SECOND_LEG_EAST_WE = Block.box(9.0D, 0.0D, 12.0D, 11.0D, 2.0D, 14.0D);

    private static final VoxelShape FACING_NS = Shapes.or(TOP_NS, MID_NS, BOTTOM_NS,
            FIRST_LEG_SUPPORT_1_NS, FIRST_LEG_SUPPORT_2_NS, FIRST_LEG_WEST_NS, FIRST_LEG_EAST_NS,
            SECOND_LEG_SUPPORT_1_NS, SECOND_LEG_SUPPORT_2_NS, SECOND_LEG_WEST_NS, SECOND_LEG_EAST_NS);

    private static final VoxelShape FACING_WE = Shapes.or(TOP_WE, MID_WE, BOTTOM_WE,
            FIRST_LEG_SUPPORT_1_WE, FIRST_LEG_SUPPORT_2_WE, FIRST_LEG_WEST_WE, FIRST_LEG_EAST_WE,
            SECOND_LEG_SUPPORT_1_WE, SECOND_LEG_SUPPORT_2_WE, SECOND_LEG_WEST_WE, SECOND_LEG_EAST_WE);

    public HighBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape shapeFor(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? FACING_NS : FACING_WE;
    }

    @Override
    protected double sitHeight() {
        return Sit.HIGH_BENCH_HEIGHT;
    }
}

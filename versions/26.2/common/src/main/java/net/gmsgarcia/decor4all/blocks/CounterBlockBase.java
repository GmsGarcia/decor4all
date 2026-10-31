package net.gmsgarcia.decor4all.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class CounterBlockBase extends WaterloggedFacingBlock {

    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    protected CounterBlockBase(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LEFT, false)
                .setValue(RIGHT, false)
                .setValue(WATERLOGGED, false));
    }

    protected BlockState applyFront(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
        return state;
    }

    protected boolean joinsInFront(BlockState state) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT, RIGHT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = PlacementFacings.horizontal(context).getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        Direction leftDir = facing.getClockWise();
        Direction rightDir = facing.getCounterClockWise();
        boolean left = isCounter(level.getBlockState(pos.relative(leftDir)));
        boolean right = isCounter(level.getBlockState(pos.relative(rightDir)));
        boolean waterlogged = level.getFluidState(pos).is(Fluids.WATER);

        return applyFront(this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(LEFT, left)
                .setValue(RIGHT, right)
                .setValue(WATERLOGGED, waterlogged), level, pos, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        Direction facing = state.getValue(FACING);
        Direction leftDir = facing.getClockWise();
        Direction rightDir = facing.getCounterClockWise();
        boolean left = isCounter(level.getBlockState(pos.relative(leftDir)));
        boolean right = isCounter(level.getBlockState(pos.relative(rightDir)));
        return applyFront(state.setValue(LEFT, left).setValue(RIGHT, right), level, pos, facing);
    }

    protected static boolean isCounter(BlockState state) {
        return state.getBlock() instanceof CounterBlockBase;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return shapeFor(state);
    }

    protected VoxelShape shapeFor(BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean left = state.getValue(LEFT);
        boolean right = state.getValue(RIGHT);

        double minX, maxX, minZ, maxZ;
        switch (facing) {
            case NORTH:
                minX = 1.0D; maxX = 15.0D;
                minZ = 1.0D; maxZ = 16.0D;
                break;
            case SOUTH:
                minX = 1.0D; maxX = 15.0D;
                minZ = 0.0D; maxZ = 15.0D;
                break;
            case EAST:
                minX = 0.0D; maxX = 15.0D;
                minZ = 1.0D; maxZ = 15.0D;
                break;
            case WEST:
                minX = 1.0D; maxX = 16.0D;
                minZ = 1.0D; maxZ = 15.0D;
                break;
            default:
                minX = 1.0D; maxX = 15.0D;
                minZ = 1.0D; maxZ = 16.0D;
        }

        Direction leftDir = facing.getClockWise();
        Direction rightDir = facing.getCounterClockWise();
        boolean joinsFront = joinsInFront(state);

        if (joinsFront) {
            switch (facing) {
                case NORTH:
                    minZ = 0.0D;
                    break;
                case SOUTH:
                    maxZ = 16.0D;
                    break;
                case EAST:
                    maxX = 16.0D;
                    break;
                case WEST:
                    minX = 0.0D;
                    break;
                default:
                    break;
            }
        }

        if (left || (joinsFront && right)) {
            if (leftDir.getAxis() == Direction.Axis.X) {
                if (leftDir == Direction.EAST) maxX = 16.0D; else minX = 0.0D;
            } else {
                if (leftDir == Direction.SOUTH) maxZ = 16.0D; else minZ = 0.0D;
            }
        }
        if (right || (joinsFront && left)) {
            if (rightDir.getAxis() == Direction.Axis.X) {
                if (rightDir == Direction.EAST) maxX = 16.0D; else minX = 0.0D;
            } else {
                if (rightDir == Direction.SOUTH) maxZ = 16.0D; else minZ = 0.0D;
            }
        }

        return Shapes.or(
                Block.box(minX, 0.0D, minZ, maxX, 13.0D, maxZ),
                Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D));
    }
}

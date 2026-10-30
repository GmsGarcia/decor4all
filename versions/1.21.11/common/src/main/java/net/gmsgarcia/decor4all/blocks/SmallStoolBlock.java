package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4all.blockentity.SmallStoolBlockEntity;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.gmsgarcia.decor4all.sit.Sit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SmallStoolBlock extends SeatBlock implements EntityBlock {

    public static final MapCodec<SmallStoolBlock> CODEC = BlockBehaviour.simpleCodec(SmallStoolBlock::new);

    private static final VoxelShape SIT = Block.box(3.0D, 6.0D, 3.0D, 13.0D, 9.0D, 13.0D);
    private static final VoxelShape FIRST_LEG = Block.box(4.0D, 0.0D, 4.0D, 6.0D, 6.0D, 6.0D);
    private static final VoxelShape SECOND_LEG = Block.box(4.0D, 0.0D, 10.0D, 6.0D, 6.0D, 12.0D);
    private static final VoxelShape THIRD_LEG = Block.box(10.0D, 0.0D, 4.0D, 12.0D, 6.0D, 6.0D);
    private static final VoxelShape FOURTH_LEG = Block.box(10.0D, 0.0D, 10.0D, 12.0D, 6.0D, 12.0D);

    private static final VoxelShape SHAPE = Shapes.or(SIT, FIRST_LEG, SECOND_LEG, THIRD_LEG, FOURTH_LEG);

    public SmallStoolBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SmallStoolBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockFamilies.WOOL_COLOR);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(BlockFamilies.WOOL_COLOR, 0);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallStoolBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape shapeFor(BlockState state) {
        return SHAPE;
    }

    @Override
    protected double sitHeight() {
        return Sit.STOOL_HEIGHT;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        int woolColor = woolColorOf(stack.getItem());
        if (woolColor != 0) {
            if (state.getValue(BlockFamilies.WOOL_COLOR) != 0) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            setWoolColor(state, level, pos, player, woolColor);
            if (level.getBlockEntity(pos) instanceof SmallStoolBlockEntity stool) {
                stool.setItem(0, stack.copyWithCount(1));
            }
            player.getItemInHand(hand).shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!player.isSecondaryUseActive()) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        if (state.getValue(BlockFamilies.WOOL_COLOR) != 0) {
            takeCarpetBack(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void setWoolColor(BlockState state, Level level, BlockPos pos, Player player, int woolColor) {
        player.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
        level.setBlock(pos, state.setValue(BlockFamilies.WOOL_COLOR, woolColor), Block.UPDATE_ALL);
    }

    private void takeCarpetBack(BlockState state, Level level, BlockPos pos, Player player) {
        setWoolColor(state, level, pos, player, 0);
        if (level.getBlockEntity(pos) instanceof SmallStoolBlockEntity stool) {
            ItemStack carpet = stool.removeItemNoUpdate(0);
            if (!carpet.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, carpet);
            }
        }
    }

    private static int woolColorOf(Item item) {
        return item instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof WoolCarpetBlock carpet
                ? carpet.getColor().getId() + 1 : 0;
    }

}

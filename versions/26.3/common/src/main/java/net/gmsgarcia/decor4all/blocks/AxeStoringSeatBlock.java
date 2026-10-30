package net.gmsgarcia.decor4all.blocks;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
public abstract class AxeStoringSeatBlock extends SeatBlock implements EntityBlock {
    protected AxeStoringSeatBlock(Properties properties) {
        super(properties);
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogBenchBlockEntity(pos, state);
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
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!isAxe(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        storeAxe(state, level, pos, player, hand, stack);
        return InteractionResult.SUCCESS;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        LogBenchBlockEntity bench = benchAt(level, pos);
        if (bench != null && !bench.isEmpty()) {
            takeAxeBack(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
    private void storeAxe(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, ItemStack stack) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity stored) {
            stored.setAxeFacing(axeFacingFor(pos, state, player));
            stored.setItem(0, stack.copyWithCount(1));
        }
        player.getItemInHand(hand).shrink(1);
    }
    private static Direction axeFacingFor(BlockPos pos, BlockState state, Player player) {
        Direction facing = state.getValue(FACING);
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        Direction towardPlayer;
        if (Math.abs(dx) >= Math.abs(dz)) {
            towardPlayer = dx > 0.0D ? Direction.EAST : Direction.WEST;
        } else {
            towardPlayer = dz > 0.0D ? Direction.SOUTH : Direction.NORTH;
        }
        if (towardPlayer.getAxis() == facing.getAxis()) {
            return facing.getAxis() == Direction.Axis.X
                    ? towardPlayer
                    : towardPlayer.getOpposite();
        }
        double across = facing.getStepX() * dx + facing.getStepZ() * dz;
        boolean readsFacing = (across > 0.0D) == (facing.getAxis() == Direction.Axis.X);
        return readsFacing ? facing : facing.getOpposite();
    }
    private void takeAxeBack(BlockState state, Level level, BlockPos pos, Player player) {
        player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench) {
            ItemStack axe = bench.removeItemNoUpdate(0);
            if (!axe.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, axe);
            }
        }
    }
    private static boolean isAxe(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }
    private static @Nullable LogBenchBlockEntity benchAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LogBenchBlockEntity bench ? bench : null;
    }
}

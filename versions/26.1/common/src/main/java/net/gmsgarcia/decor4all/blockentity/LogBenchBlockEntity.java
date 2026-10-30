package net.gmsgarcia.decor4all.blockentity;

import net.gmsgarcia.decor4all.content.DecorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class LogBenchBlockEntity extends SingleSlotBlockEntity {

    private static final String KEY_AXE_FACING = "AxeFacing";

    private @Nullable Direction axeFacing;

    public LogBenchBlockEntity(BlockPos pos, BlockState state) {
        super(type(), pos, state);
    }

    public @Nullable Direction axeFacing() {
        return this.axeFacing;
    }

    public void setAxeFacing(@Nullable Direction facing) {
        this.axeFacing = facing;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.axeFacing = input.getInt(KEY_AXE_FACING).map(Direction::from2DDataValue).orElse(null);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.axeFacing != null) {
            output.putInt(KEY_AXE_FACING, this.axeFacing.get2DDataValue());
        }
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<LogBenchBlockEntity> type() {
        return (BlockEntityType<LogBenchBlockEntity>) (BlockEntityType<?>)
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(DecorBlocks.LOG_BENCH.key());
    }
}

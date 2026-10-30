package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LogFenceBlock extends FenceBlock {

    public static final MapCodec<FenceBlock> CODEC = BlockBehaviour.simpleCodec(LogFenceBlock::new);

    public LogFenceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }
}

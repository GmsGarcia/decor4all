package net.gmsgarcia.decor4all.blocks;

import com.mojang.serialization.MapCodec;
import net.gmsgarcia.decor4all.blockentity.DrawerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class DrawerCounterBlock extends ContainerCounterBlock {

    public static final MapCodec<DrawerCounterBlock> CODEC = BlockBehaviour.simpleCodec(DrawerCounterBlock::new);

    public DrawerCounterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DrawerCounterBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrawerBlockEntity(pos, state);
    }
}

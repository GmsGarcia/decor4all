package net.gmsgarcia.decor4all.sit;

import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class Sit {

    public static final double BENCH_HEIGHT = 0.37D;

    public static final double HIGH_BENCH_HEIGHT = 0.50D;

    public static final double CHAIR_HEIGHT = 0.55D;

    public static final double STOOL_HEIGHT = 0.55D;

    private Sit() {
    }

    public static InteractionResult trySit(Player player, Level level, BlockPos pos, double height) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockFamilies.OCCUPIED) || state.getValue(BlockFamilies.OCCUPIED)) {
            return InteractionResult.PASS;
        }
        EntityType<SitEntity> type = sitEntityType();
        if (type == null) {
            return InteractionResult.PASS;
        }
        SitEntity sit = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (sit == null) {
            return InteractionResult.PASS;
        }

        sit.attach(pos, player.blockPosition());
        sit.setPos(pos.getX() + 0.5D, pos.getY() + height, pos.getZ() + 0.5D);
        if (!level.addFreshEntity(sit)) {
            return InteractionResult.PASS;
        }
        if (!player.startRiding(sit, true, true)) {
            sit.discard();
            return InteractionResult.PASS;
        }
        level.setBlock(pos, state.setValue(BlockFamilies.OCCUPIED, true), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    private static EntityType<SitEntity> sitEntityType() {
        return Decor4All.sitEntityType();
    }
}

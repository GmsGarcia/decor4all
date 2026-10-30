package net.gmsgarcia.decor4all.sit;

import java.util.Optional;
import net.gmsgarcia.decor4all.content.BlockFamilies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SitEntity extends Entity {

    private static final String KEY_SEAT_X = "SeatX";
    private static final String KEY_SEAT_Y = "SeatY";
    private static final String KEY_SEAT_Z = "SeatZ";
    private static final String KEY_ANCHOR_X = "AnchorX";
    private static final String KEY_ANCHOR_Y = "AnchorY";
    private static final String KEY_ANCHOR_Z = "AnchorZ";

    private @Nullable BlockPos seatPos;

    private @Nullable BlockPos anchorPos;

    private boolean released;

    public SitEntity(EntityType<? extends SitEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void attach(BlockPos seat, BlockPos anchor) {
        this.seatPos = seat.immutable();
        this.anchorPos = anchor.immutable();
    }

    public @Nullable BlockPos seatPos() {
        return this.seatPos;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.seatPos == null || this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }
        BlockState state = this.level().getBlockState(this.seatPos);
        if (!state.hasProperty(BlockFamilies.OCCUPIED) || !state.getValue(BlockFamilies.OCCUPIED)) {
            this.discard();
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos anchor = this.anchorPos;
        this.discard();
        if (passenger instanceof Player && anchor != null) {
            return Vec3.atCenterOf(anchor);
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void remove(RemovalReason reason) {
        this.releaseSeat();
        super.remove(reason);
    }

    private void releaseSeat() {
        if (this.released) {
            return;
        }
        this.released = true;
        BlockPos pos = this.seatPos;
        Level level = this.level();
        if (pos == null || level == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockFamilies.OCCUPIED) && state.getValue(BlockFamilies.OCCUPIED)) {
            level.setBlock(pos, state.setValue(BlockFamilies.OCCUPIED, false), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.seatPos != null) {
            output.putInt(KEY_SEAT_X, this.seatPos.getX());
            output.putInt(KEY_SEAT_Y, this.seatPos.getY());
            output.putInt(KEY_SEAT_Z, this.seatPos.getZ());
        }
        if (this.anchorPos != null) {
            output.putInt(KEY_ANCHOR_X, this.anchorPos.getX());
            output.putInt(KEY_ANCHOR_Y, this.anchorPos.getY());
            output.putInt(KEY_ANCHOR_Z, this.anchorPos.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.seatPos = readPos(input, KEY_SEAT_X, KEY_SEAT_Y, KEY_SEAT_Z);
        this.anchorPos = readPos(input, KEY_ANCHOR_X, KEY_ANCHOR_Y, KEY_ANCHOR_Z);
    }

    private static @Nullable BlockPos readPos(ValueInput input, String x, String y, String z) {
        Optional<Integer> px = input.getInt(x);
        Optional<Integer> py = input.getInt(y);
        Optional<Integer> pz = input.getInt(z);
        if (px.isEmpty() || py.isEmpty() || pz.isEmpty()) {
            return null;
        }
        return new BlockPos(px.get(), py.get(), pz.get());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}

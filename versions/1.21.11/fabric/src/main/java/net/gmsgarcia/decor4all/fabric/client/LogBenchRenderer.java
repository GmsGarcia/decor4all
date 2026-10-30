package net.gmsgarcia.decor4all.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class LogBenchRenderer implements BlockEntityRenderer<LogBenchBlockEntity, LogBenchRenderer.State> {

    static final float AXE_SPIN_DEGREES = 90.0F;

    static final float AXE_LEAN_DEGREES = 270.0F;

    static final float AXE_OFFSET_X = -0.3F;
    static final float AXE_OFFSET_Y = -0.15F;
    static final float AXE_OFFSET_Z = 0.0F;

    static final float AXE_SCALE = 0.65F;

    public LogBenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static final class State extends BlockEntityRenderState {

        public final ItemStackRenderState axe = new ItemStackRenderState();

        public float yaw;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LogBenchBlockEntity bench, State state, float partialTick,
            Vec3 cameraPos, CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bench, state, partialTick, cameraPos, crumblingOverlay);

        ItemStack stack = bench.getItem(0);
        state.axe.clear();
        if (stack.isEmpty()) {
            return;
        }
        Direction facing = bench.axeFacing() != null
                ? bench.axeFacing()
                : bench.getBlockState()
                        .getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
                        .orElse(Direction.NORTH);
        state.yaw = yawFor(facing);
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.axe, stack, ItemDisplayContext.FIXED, bench.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.axe.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.YP.rotationDegrees(AXE_SPIN_DEGREES));
        poseStack.translate(AXE_OFFSET_X, AXE_OFFSET_Y, AXE_OFFSET_Z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(AXE_LEAN_DEGREES));
        float s = AXE_SCALE;
        poseStack.scale(s, s, s);
        state.axe.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float yawFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}

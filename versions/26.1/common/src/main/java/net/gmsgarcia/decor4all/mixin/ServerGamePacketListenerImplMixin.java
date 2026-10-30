package net.gmsgarcia.decor4all.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    private Vec3 awaitingPositionFromClient;

    @Redirect(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendBuildLimitMessage(ZI)V",
                    ordinal = 5))
    private void decor4all$suppressFalseBuildLimitOnMount(
            ServerPlayer player, boolean isTooHigh, int limit) {
        if (this.awaitingPositionFromClient == null) {
            player.sendBuildLimitMessage(isTooHigh, limit);
        }
    }
}

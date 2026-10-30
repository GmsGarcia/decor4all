package net.gmsgarcia.decor4all.fabric.client;

import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.gmsgarcia.decor4all.Decor4All;
import net.gmsgarcia.decor4all.blockentity.LogBenchBlockEntity;
import net.gmsgarcia.decor4all.net.CarpenterTableRecipesPayload;
import net.minecraft.client.gui.screens.MenuScreens;

public class FabricDecor4AllClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                Objects.requireNonNull(Decor4All.sitEntityType(), "sit entity type not registered"),
                SitEntityRenderer::new);
        BlockEntityRendererRegistry.register(LogBenchBlockEntity.type(), LogBenchRenderer::new);
        MenuScreens.register(Decor4All.carpenterTableMenuType(), CarpenterTableScreen::new);

        PayloadTypeRegistry.playS2C().register(CarpenterTableRecipesPayload.TYPE, CarpenterTableRecipesPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CarpenterTableRecipesPayload.TYPE,
                (payload, context) -> CarpenterTableClientRecipes.accept(payload));

        Decor4All.LOGGER.info("Decor4All client ready");
    }
}

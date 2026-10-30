package net.gmsgarcia.decor4all.fabric.client;

import net.gmsgarcia.decor4all.sit.SitEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class SitEntityRenderer extends EntityRenderer<SitEntity, EntityRenderState> {

    public SitEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public boolean shouldRender(SitEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }
}

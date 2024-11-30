package com.bread.vehiclemod.vehicles.renderers;

import com.bread.vehiclemod.vehicles.entities.Annihilator;
import com.bread.vehiclemod.vehicles.models.AnnihilatorModel;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AnnihilatorRenderer extends AbstractWheeledVehicleRenderer<Annihilator> {
    public AnnihilatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AnnihilatorModel());
    }

    @Override
    public void render(Annihilator entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}

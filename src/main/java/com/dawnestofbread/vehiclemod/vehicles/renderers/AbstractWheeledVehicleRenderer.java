package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.utils.Maths;
import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.Nullable;
import java.util.Date;

public abstract class AbstractWheeledVehicleRenderer<T extends WheeledVehicle> extends AbstractVehicleRenderer<T> {
    public AbstractWheeledVehicleRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    protected long lastFrame;

    @Override
    public void render(@Nullable T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity == null) return;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
        lastFrame = new Date().getTime();

        GeoBone body = this.getGeoModel().getBone("body").orElse(null);
        if (body != null) {
            float newXBodyRot = Maths.fInterpToExp(body.getRotX(), (float) (entity.weightTransferX * entity.maxBodyPitch) * (Mth.PI/180), 1.5f, (float) deltaTime);
            float newZBodyRot = Maths.fInterpToExp(body.getRotZ(), (float) (entity.weightTransferZ * entity.maxBodyRoll) * (Mth.PI/180), 1.5f, (float) deltaTime);
            body.setRotX(newXBodyRot);
            body.setRotZ(newZBodyRot);
            entity.passengerXAdditional = newXBodyRot / (Mth.PI/180);
            entity.passengerZAdditional = newZBodyRot / (Mth.PI/180);
        }

        for (int i = 0; i < entity.Wheels.length; i++) {
            GeoBone bone = this.getGeoModel().getBone("wheel" + String.valueOf(i)).orElse(null);
            if (bone != null) {
                bone.setRotX(bone.getRotX() - (float) (entity.Wheels[i].angularVelocity * deltaTime));
                if (entity.Wheels[i].affectedBySteering) bone.setRotY(Maths.fInterpToExp(bone.getRotY(), (float) - (entity.steering * entity.steeringAngle) * (Mth.PI/180), 4.25f, (float) deltaTime));
            }

        }
        this.drawLine(poseStack, bufferSource, entity.position().add(entity.frontAxle), entity.position().add(entity.rearAxle), 0xFF0000, 0xFF00FF);
        for (int i = 0; i < entity.Wheels.length; i++) {
            this.drawLine(poseStack, bufferSource, entity.position().add(entity.Wheels[i].startingRelativePosition), entity.position().add(entity.Wheels[i].startingRelativePosition).add(entity.Wheels[i].startingRelativePosition.x < 0 ? -1 : 1,0,0), 0xFF0000, 0xFF00FF);
            this.drawLine(poseStack, bufferSource, entity.position().add(entity.Wheels[i].startingRelativePosition), entity.position().add(entity.Wheels[i].startingRelativePosition).subtract(0,entity.Wheels[i].radius,0), 0xFF0000, 0xFF00FF);
        }
    }
}

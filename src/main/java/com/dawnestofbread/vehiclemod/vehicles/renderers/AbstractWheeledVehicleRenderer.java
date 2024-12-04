package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.WheelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.Nullable;

public abstract class AbstractWheeledVehicleRenderer<T extends WheeledVehicle> extends AbstractVehicleRenderer<T> {
    public AbstractWheeledVehicleRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    @Override
    public void render(@Nullable T entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity == null) return;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // Transform PoseStack
        poseStack.pushPose();
        poseStack.translate(-entity.getX(), -entity.getY(), -entity.getZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));

        // Create a debug render buffer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        for (WheelData wheel : entity.Wheels) {
            Vec3 offsetStart = entity.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(-wheel.width / 2, -wheel.radius,-wheel.radius).yRot(-entity.getYRot() * ((float)Math.PI / 180F)).xRot(-entity.getXRot() * ((float)Math.PI / 180F))));
            Vec3 offsetEnd = entity.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(wheel.width / 2, wheel.radius,wheel.radius).yRot(-entity.getYRot() * ((float)Math.PI / 180F)).xRot(-entity.getXRot() * ((float)Math.PI / 180F))));
            renderBox(poseStack, vertexConsumer, new AABB(offsetStart.x , offsetStart.y, offsetStart.z, offsetEnd.x, offsetEnd.y, offsetEnd.z), 1,0,0);
        }
        Vec3 offsetStart = new Vec3(-entity.width / 2, 0, -entity.length / 2);
        Vec3 offsetEnd = new Vec3(entity.width / 2, entity.height, entity.length / 2);
        renderBox(poseStack, vertexConsumer, new AABB(offsetStart.x , offsetStart.y, offsetStart.z, offsetEnd.x, offsetEnd.y, offsetEnd.z), 0,0,1);

        poseStack.popPose();
    }
}

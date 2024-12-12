package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractVehicleRenderer<T extends AbstractVehicle> extends GeoEntityRenderer<T> {
    public AbstractVehicleRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.shadowRadius = 0;
    }
    protected GeoModel<T> model;

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        //poseStack.translate(animatable.translateOffset.x, animatable.translateOffset.y, animatable.translateOffset.z);
    }

    protected void renderBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aabb, float red, float green, float blue) {
        PoseStack.Pose pose = poseStack.last();

        // Bottom edges
        drawLine(vertexConsumer, pose, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.minZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.minY, aabb.maxZ, aabb.minX, aabb.minY, aabb.maxZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.minX, aabb.minY, aabb.maxZ, aabb.minX, aabb.minY, aabb.minZ, red, green, blue);

        // Top edges
        drawLine(vertexConsumer, pose, aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.maxY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.maxZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.minX, aabb.maxY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.minZ, red, green, blue);

        // Vertical edges
        drawLine(vertexConsumer, pose, aabb.minX, aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.minZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.maxX, aabb.minY, aabb.maxZ, aabb.maxX, aabb.maxY, aabb.maxZ, red, green, blue);
        drawLine(vertexConsumer, pose, aabb.minX, aabb.minY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.maxZ, red, green, blue);
    }

    protected void drawLine(VertexConsumer vertexConsumer, PoseStack.Pose pose, double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue) {
        vertexConsumer.vertex(pose.pose(), (float) x1, (float) y1, (float) z1).color(red, green, blue, 1.0f).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose.pose(), (float) x2, (float) y2, (float) z2).color(red, green, blue, 1.0f).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }
    protected void drawLine(VertexConsumer vertexConsumer, PoseStack.Pose pose, Vec3 vec1, Vec3 vec2, float red, float green, float blue) {
        vertexConsumer.vertex(pose.pose(), (float) vec1.x, (float) vec1.y, (float) vec1.z).color(red, green, blue, 1.0f).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(pose.pose(), (float) vec2.x, (float) vec2.y, (float) vec2.z).color(red, green, blue, 1.0f).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }
}

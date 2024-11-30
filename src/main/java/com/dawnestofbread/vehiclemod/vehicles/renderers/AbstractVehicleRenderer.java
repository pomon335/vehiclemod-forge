package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public abstract class AbstractVehicleRenderer<T extends AbstractVehicle> extends GeoEntityRenderer<T> {
    public AbstractVehicleRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.shadowRadius = 0;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        //poseStack.translate(animatable.translateOffset.x, animatable.translateOffset.y, animatable.translateOffset.z);
    }

    public void drawLine(PoseStack stack, MultiBufferSource bufferSource, Vec3 from, Vec3 to, int startColour, int endColour)
    {
        RenderSystem.lineWidth(5.0f);
        RenderSystem.enableDepthTest();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.LINES);

        Matrix4f matrix = stack.last().pose();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        float sred = (float) (startColour >> 16 & 255) / 255.0F;
        float sgreen = (float) (startColour >> 8 & 255) / 255.0F;
        float sblue = (float) (startColour & 255) / 255.0F;
        float ered = (float) (endColour >> 16 & 255) / 255.0F;
        float egreen = (float) (endColour >> 8 & 255) / 255.0F;
        float eblue = (float) (endColour & 255) / 255.0F;

        from = from.subtract(camera.getPosition());
        to = to.subtract(camera.getPosition());

        // Add vertices for the line
        builder.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .color(sred, sgreen, sblue, 1f)
                .normal(0,0,0)
                .endVertex();
        builder.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .color(ered, egreen, eblue, 1f)
                .normal(0,0,0)
                .endVertex();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableDepthTest();
    }
}

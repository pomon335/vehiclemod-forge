package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.WheelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.Nullable;
import java.util.List;

import static com.dawnestofbread.vehiclemod.utils.VectorUtils.rotateVectorToEntitySpace;
import static com.dawnestofbread.vehiclemod.utils.VectorUtils.rotateVectorToEntitySpaceYOnly;

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

        // Create a debug render buffer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        for (WheelData wheel : entity.Wheels) {
            Vec3 offsetStart = entity.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(-wheel.width / 2, -wheel.radius, -wheel.radius).yRot(-entity.getYRot() * ((float) Math.PI / 180F)).xRot(-entity.getXRot() * ((float) Math.PI / 180F))));
            Vec3 offsetEnd = entity.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(wheel.width / 2, wheel.radius, wheel.radius).yRot(-entity.getYRot() * ((float) Math.PI / 180F)).xRot(-entity.getXRot() * ((float) Math.PI / 180F))));

            Vec3 lineTraceStart = entity.position().add(rotateVectorToEntitySpace(wheel.startingRelativePosition.scale(.5).add(0, -wheel.springMinLength, 0), entity));
            Vec3 lineTraceEnd = entity.position().add(rotateVectorToEntitySpace(wheel.startingRelativePosition.scale(.5).add(0, wheel.springMaxLength, 0), entity));
            drawLine(vertexConsumer, poseStack.last(), lineTraceStart, lineTraceEnd, wheel.onGround ? 0 : 1, wheel.onGround ? 1 : 0, 0);

            Vec3 lineTraceStart1 = entity.position().add(rotateVectorToEntitySpaceYOnly(wheel.startingRelativePosition.scale(.5).add(0, entity.climbAmount * 1.01, 0), entity));
            Vec3 lineTraceEnd1 = lineTraceStart1.add(0, -entity.wheelBase - wheel.radius, 0).add(rotateVectorToEntitySpaceYOnly(new Vec3(0, 0, wheel.radius * 1.5 * (entity.getForwardSpeed() > 0 ? 1f : entity.getForwardSpeed() < 0 ? -1f : 0f)), entity));
            drawLine(vertexConsumer, poseStack.last(), lineTraceStart1, lineTraceEnd1, 0, 0, 1);

            renderBox(poseStack, vertexConsumer, new AABB(wheel.targetWorldPosition.x - .25, wheel.targetWorldPosition.y - .25, wheel.targetWorldPosition.z - .25, wheel.targetWorldPosition.x + .25, wheel.targetWorldPosition.y + .25, wheel.targetWorldPosition.z + .25), 1, 0, 1);
        }
        int wheelCount = entity.Wheels.size();
        List<WheelData> frontWheels = entity.Wheels.subList(0, wheelCount / 2); // Front wheels (first half)
        List<WheelData> rearWheels = entity.Wheels.subList(wheelCount / 2, wheelCount); // Rear wheels (second half)

        Vec3 frontMidpoint = entity.calculateAxleMidpoint(frontWheels);
        Vec3 rearMidpoint = entity.calculateAxleMidpoint(rearWheels);
        drawLine(vertexConsumer, poseStack.last(), rearMidpoint, new Vec3(frontMidpoint.x, rearMidpoint.y, frontMidpoint.z), 1, 1, 0);
        drawLine(vertexConsumer, poseStack.last(), frontMidpoint, new Vec3(frontMidpoint.x, rearMidpoint.y, frontMidpoint.z), 1, 1, 0);

        poseStack.popPose();
    }
}

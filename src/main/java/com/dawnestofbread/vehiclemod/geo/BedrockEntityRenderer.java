package com.dawnestofbread.vehiclemod.geo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class BedrockEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private final JsonObject bedrockModel;

    public BedrockEntityRenderer(EntityRendererProvider.Context context, ResourceLocation modelLocation) {
        super(context);
        this.bedrockModel = loadModel(modelLocation);
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        if (bedrockModel == null) {
            return;
        }

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        // Render the Bedrock model parts.
        renderBedrockModel(poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    private void renderBedrockModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(null)));

        for (JsonElement part : bedrockModel.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject().getAsJsonArray("bones")) {
            renderBone(part.getAsJsonObject(), poseStack, vertexConsumer, packedLight);
        }
    }

    private void renderBone(JsonObject bone, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {

        // Render all cubes for this bone.
        if (bone.has("cubes")) {
            for (JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
                JsonObject cube = cubeElement.getAsJsonObject();
                // Apply the bone's pivot (translation).
                poseStack.pushPose();
                if (bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    poseStack.translate(pivot.get(0).getAsFloat() / 16, pivot.get(1).getAsFloat() / 16, pivot.get(2).getAsFloat() / 16);
                }
                renderCube(cube, poseStack, vertexConsumer, packedLight);
            }
        }

        //poseStack.popPose();
    }

    private void renderCube(JsonObject cube, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        // Extract cube properties
        JsonArray origin = cube.getAsJsonArray("origin");
        JsonArray size = cube.getAsJsonArray("size");
        JsonArray pivot = cube.has("pivot") ? cube.getAsJsonArray("pivot") : null;
        JsonArray rotation = cube.has("rotation") ? cube.getAsJsonArray("rotation") : null;

        float originX = origin.get(0).getAsFloat() / 16;
        float originY = origin.get(1).getAsFloat() / 16;
        float originZ = origin.get(2).getAsFloat() / 16;

        float sizeX = size.get(0).getAsFloat() / 16;
        float sizeY = size.get(1).getAsFloat() / 16;
        float sizeZ = size.get(2).getAsFloat() / 16;

        //poseStack.pushPose();

        // Apply pivot and rotation if specified
        if (pivot != null) {
            poseStack.translate(pivot.get(0).getAsFloat()  / 16, pivot.get(1).getAsFloat() / 16, pivot.get(2).getAsFloat() / 16);
        }
        if (rotation != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation.get(0).getAsFloat()));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation.get(1).getAsFloat()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.get(2).getAsFloat()));
        }

        // Render the cube
        renderBox(vertexConsumer, poseStack, originX, originY, originZ, sizeX, sizeY, sizeZ, packedLight);

        poseStack.popPose();
    }

    private void renderBox(VertexConsumer vertexConsumer, PoseStack poseStack,
                           float originX, float originY, float originZ,
                           float sizeX, float sizeY, float sizeZ,
                           int packedLight) {
        PoseStack.Pose pose = poseStack.last();

        // Define vertices for each face
        float x1 = originX;
        float y1 = originY;
        float z1 = originZ;

        float x2 = originX + sizeX;
        float y2 = originY + sizeY;
        float z2 = originZ + sizeZ;

        // Front (north) face
        renderFace(vertexConsumer, pose, x1, y1, z2, x2, y2, z2, packedLight);

        // Back (south) face
        renderFace(vertexConsumer, pose, x2, y1, z1, x1, y2, z1, packedLight);

        // Left (west) face
        renderFace(vertexConsumer, pose, x1, y1, z1, x1, y2, z2, packedLight);

        // Right (east) face
        renderFace(vertexConsumer, pose, x2, y1, z2, x2, y2, z1, packedLight);

        // Top face
        renderFace(vertexConsumer, pose, x1, y2, z1, x2, y2, z2, packedLight);

        // Bottom face
        renderFace(vertexConsumer, pose, x1, y1, z2, x2, y1, z1, packedLight);
    }

    private void renderFace(VertexConsumer vertexConsumer, PoseStack.Pose pose,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            int packedLight) {
        // Calculate the normals from cross products
        Vector3f[] normals = new Vector3f[4];
        normals[0] = new Vector3f(x2, y1, z1).sub(x1, y1, z1).cross(new Vector3f(x1, y2, z2).sub(x1, y1, z1)).normalize();
        normals[1] = new Vector3f(x1, y1, z1).sub(x2, y1, z1).cross(new Vector3f(x1, y2, z2).sub(x2, y1, z1)).normalize();
        normals[2] = new Vector3f(x1, y1, z1).sub(x1, y2, z2).cross(new Vector3f(x2, y1, z1).sub(x1, y2, z2)).normalize();
        normals[3] = new Vector3f(x2, y1, z1).sub(x2, y2, z2).cross(new Vector3f(x1, y2, z2).sub(x2, y2, z2)).normalize().mul(-1);
        normals[3] = new Vector3f(normals[2].z, normals[2].y, 0);
        // Define four vertices of the face
        vertex(vertexConsumer, pose, x1, y1, z1, 0, 0, packedLight, normals[0]);
        vertex(vertexConsumer, pose, x2, y1, z1, 1, 0, packedLight, normals[2]);
        vertex(vertexConsumer, pose, x1, y2, z2, 0, 1, packedLight, normals[1]);
        vertex(vertexConsumer, pose, x2, y2, z2, 1, 1, packedLight, normals[3]);
    }

    private void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose,
                        float x, float y, float z,
                        float u, float v, int packedLight, Vector3f normal) {
        vertexConsumer.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)  // White color for now
                .uv(u, v)                                                   // UV coordinates
                .overlayCoords(0)                                   // No overlay
                .uv2(packedLight)                                           // Lightmap coordinates
                .normal(pose.normal(), normal.x, normal.y, normal.z)        // Normal vector
                .endVertex();
    }

    private JsonObject loadModel(ResourceLocation modelLocation) {
        try {
            InputStream stream = Minecraft.getInstance().getResourceManager().open(modelLocation);
            InputStreamReader reader = new InputStreamReader(stream);
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return new ResourceLocation("vehiclemod", "textures/twinkie.png");
    }
}


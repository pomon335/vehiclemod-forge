package com.bread.vehiclemod.vehicles.models;

import com.bread.vehiclemod.AbstractVehicle;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class VehicleModel extends ListModel<AbstractVehicle> {
    private final ModelPart body;
    private final ModelPart frontLeftWheel;
    private final ModelPart frontRightWheel;
    private final ModelPart backLeftWheel;
    private final ModelPart backRightWheel;

    public VehicleModel(ModelPart root) {
        // Initialize parts from the root
        this.body = root.getChild("body");
        this.frontLeftWheel = root.getChild("front_left_wheel");
        this.frontRightWheel = root.getChild("front_right_wheel");
        this.backLeftWheel = root.getChild("back_left_wheel");
        this.backRightWheel = root.getChild("back_right_wheel");
    }

    public static LayerDefinition createBodyLayer() {
        // Define the structure of the vehicle
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body of the vehicle
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -6.0F, -14.0F, 16.0F, 6.0F, 28.0F), // A simple rectangular car body
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        // Wheels
        root.addOrReplaceChild("front_left_wheel",
                CubeListBuilder.create()
                        .texOffs(0, 34)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), // A simple wheel
                PartPose.offset(7.0F, 22.0F, -10.0F)
        );

        root.addOrReplaceChild("front_right_wheel",
                CubeListBuilder.create()
                        .texOffs(0, 34)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(-7.0F, 22.0F, -10.0F)
        );

        root.addOrReplaceChild("back_left_wheel",
                CubeListBuilder.create()
                        .texOffs(0, 34)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(7.0F, 22.0F, 10.0F)
        );

        root.addOrReplaceChild("back_right_wheel",
                CubeListBuilder.create()
                        .texOffs(0, 34)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(-7.0F, 22.0F, 10.0F)
        );

        return LayerDefinition.create(mesh, 64, 64); // Texture size
    }

    @Override
    public Iterable<ModelPart> parts() {
        // Return all the model parts for rendering
        return ImmutableList.of(body, frontLeftWheel, frontRightWheel, backLeftWheel, backRightWheel);
    }

    @Override
    public void setupAnim(AbstractVehicle entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Animate the wheels (simple rotation example)
        float wheelRotation = limbSwing * 20.0F; // Adjust rotation speed
        this.frontLeftWheel.yRot = wheelRotation;
        this.frontRightWheel.yRot = wheelRotation;
        this.backLeftWheel.yRot = wheelRotation;
        this.backRightWheel.yRot = wheelRotation;
    }
}


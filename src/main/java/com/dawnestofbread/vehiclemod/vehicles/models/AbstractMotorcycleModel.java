package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.Maths;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

import java.util.Date;

public abstract class AbstractMotorcycleModel<T extends WheeledVehicle> extends AbstractWheeledVehicleModel<T> {
    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> animationState) {
        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
        lastFrame = new Date().getTime();

        CoreGeoBone root = this.getAnimationProcessor().getBone("root");
        CoreGeoBone body = this.getAnimationProcessor().getBone("body");
        if (body != null && root != null) {
            root.setRotX(entity.getXRot());

            float newXBodyRot = Maths.fInterpToExp(body.getRotX(), (float) (entity.weightTransferX * entity.maxBodyPitch) * (Mth.PI/180), 1.5f, (float) deltaTime);
            float newZRootRot = Maths.fInterpToExp(root.getRotZ(), (float) (entity.weightTransferZ * -entity.maxBodyRoll * 2) * (Mth.PI/180), 4.25f, (float) deltaTime);
            body.setRotX(newXBodyRot);
            root.setRotZ(newZRootRot);
            entity.passengerXAdditional = newXBodyRot / (Mth.PI/180);
            entity.passengerZAdditional = newZRootRot / (Mth.PI/180);
        }

        CoreGeoBone additionalTurningComponent = this.getAnimationProcessor().getBone("additionalTurningComponent");
        for (int i = 0; i < entity.Wheels.length; i++) {
            CoreGeoBone bone = this.getAnimationProcessor().getBone("wheel" + String.valueOf(i));
            if (bone != null) {
                bone.setRotX(bone.getRotX() - (float) (entity.Wheels[i].angularVelocity * deltaTime));
                if (entity.Wheels[i].affectedBySteering) {
                    bone.setRotY(Maths.fInterpToExp(bone.getRotY(), (float) -(entity.steering * entity.steeringAngle * .5f) * (Mth.PI/180), 4.25f, (float) deltaTime));
                    if (additionalTurningComponent != null) {
                        additionalTurningComponent.setRotX(Maths.fInterpToExp(additionalTurningComponent.getRotX(), (float) (-(entity.steering * entity.steeringAngle) * 0.0031f + 22.5) * (Mth.PI/180), 4.25f, (float) deltaTime));
                        additionalTurningComponent.setRotY(Maths.fInterpToExp(additionalTurningComponent.getRotY(), (float) -(entity.steering * entity.steeringAngle) * 0.9239f * (Mth.PI/180), 4.25f, (float) deltaTime));
                        additionalTurningComponent.setRotZ(Maths.fInterpToExp(additionalTurningComponent.getRotZ(), (float) -(entity.steering * entity.steeringAngle) * 0.3827f * (Mth.PI/180), 4.25f, (float) deltaTime));
                    };
                }
            }
        }
    }
}

package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.MathUtils;
import com.eliotlash.mclib.math.Variable;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.molang.MolangQueries;

import java.util.Date;

public abstract class AbstractMotorcycleModel<T extends WheeledVehicle> extends AbstractWheeledVehicleModel<T> {
 /*   @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> animationState) {
        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
        lastFrame = new Date().getTime();

        CoreGeoBone root = this.getAnimationProcessor().getBone("root");
        CoreGeoBone body = this.getAnimationProcessor().getBone("body");
        if (body != null && root != null) {
            float newRootXRot = MathUtils.fInterpTo(root.getRotX(), (float) Math.toRadians(entity.getXRot()), 1.5f, (float) deltaTime);
            root.setRotX(newRootXRot);
            root.setPosY((float) entity.getTranslationOffset().y);

            float newXBodyRot = MathUtils.fInterpToExp(body.getRotX(), (float) (entity.weightTransferX * entity.maxBodyPitch) * (Mth.PI / 180), 1.5f, (float) deltaTime);
            float newZRootRot = MathUtils.fInterpToExp(root.getRotZ(), (float) (entity.weightTransferZ * -entity.maxBodyRoll * 2) * (Mth.PI / 180), 4.25f, (float) deltaTime);
            body.setRotX(newXBodyRot);
            root.setRotZ(newZRootRot);
            entity.passengerXRot = (float) (Math.toDegrees(newXBodyRot) + Math.toDegrees(newRootXRot));
            entity.passengerZRot = newZRootRot / (Mth.PI / 180);
        }

        CoreGeoBone additionalTurningComponent = this.getAnimationProcessor().getBone("additionalTurningComponent");
        for (int i = 0; i < entity.Wheels.size(); i++) {
            CoreGeoBone bone = this.getAnimationProcessor().getBone("wheel" + i);
            if (bone != null) {
                bone.setPosY((float) -entity.Wheels.get(i).springLength * 8);
                bone.setRotX(bone.getRotX() - (float) (entity.Wheels.get(i).angularVelocity * deltaTime));
                if (entity.Wheels.get(i).affectedBySteering) {
                    bone.setRotY(MathUtils.fInterpToExp(bone.getRotY(), (float) -(entity.steering * entity.steeringAngle * .5f) * (Mth.PI / 180), 4.25f, (float) deltaTime));
                    if (additionalTurningComponent != null) {
                        additionalTurningComponent.setRotX(MathUtils.fInterpToExp(additionalTurningComponent.getRotX(), (float) (-(entity.steering * entity.steeringAngle) * 0.0031f + 22.5) * (Mth.PI / 180), 4.25f, (float) deltaTime));
                        additionalTurningComponent.setRotY(MathUtils.fInterpToExp(additionalTurningComponent.getRotY(), (float) -(entity.steering * entity.steeringAngle) * 0.9239f * (Mth.PI / 180), 4.25f, (float) deltaTime));
                        additionalTurningComponent.setRotZ(MathUtils.fInterpToExp(additionalTurningComponent.getRotZ(), (float) -(entity.steering * entity.steeringAngle) * 0.3827f * (Mth.PI / 180), 4.25f, (float) deltaTime));
                    }
                }
            }
        }
    }*/
}

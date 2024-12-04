package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.Maths;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

import java.util.Date;

import static com.dawnestofbread.vehiclemod.VehicleMod.LOGGER;

public abstract class AbstractWheeledVehicleModel<T extends WheeledVehicle> extends AbstractVehicleModel<T> {
    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
        lastFrame = new Date().getTime();

        CoreGeoBone root = this.getAnimationProcessor().getBone("root");
        CoreGeoBone body = this.getAnimationProcessor().getBone("body");
        if (body != null && root != null) {
            root.setRotX(entity.getXRot());

            float newXBodyRot = Maths.fInterpToExp(body.getRotX(), (float) (entity.weightTransferX * entity.maxBodyPitch) * (Mth.PI/180), 1.5f, (float) deltaTime);
            float newZBodyRot = Maths.fInterpToExp(body.getRotZ(), (float) (entity.weightTransferZ * entity.maxBodyRoll) * (Mth.PI/180), 1.5f, (float) deltaTime);
            body.setRotX(newXBodyRot);
            body.setRotZ(newZBodyRot);
            entity.passengerXAdditional = newXBodyRot / (Mth.PI/180);
            entity.passengerZAdditional = newZBodyRot / (Mth.PI/180);
        }

        for (int i = 0; i < entity.Wheels.length; i++) {
            CoreGeoBone bone = this.getAnimationProcessor().getBone("wheel" + i);
            if (bone != null) {
                bone.setRotX(bone.getRotX() - (float) (entity.Wheels[i].angularVelocity * deltaTime));
                if (entity.Wheels[i].affectedBySteering) bone.setRotY(Maths.fInterpToExp(bone.getRotY(), (float) - (entity.steering * entity.steeringAngle) * (Mth.PI/180), 4.25f, (float) deltaTime));
            }

        }
    }
}

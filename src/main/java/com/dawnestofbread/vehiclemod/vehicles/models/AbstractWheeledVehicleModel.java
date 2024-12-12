package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import com.dawnestofbread.vehiclemod.utils.MathUtils;
import com.dawnestofbread.vehiclemod.utils.WheelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Date;

import static com.dawnestofbread.vehiclemod.VehicleMod.LOGGER;

public abstract class AbstractWheeledVehicleModel<T extends WheeledVehicle> extends AbstractVehicleModel<T> {
    protected float bodyRoll;
    protected float bodyPitch;
    protected float rootRoll;
    protected float rootPitch;
    private final MolangParser parser = MolangParser.INSTANCE;
/*    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);



        CoreGeoBone root = this.getAnimationProcessor().getBone("root");
        CoreGeoBone body = this.getAnimationProcessor().getBone("body");
        if (body != null && root != null) {
            float newRootXRot = MathUtils.fInterpTo(root.getRotX(), (float) Math.toRadians(entity.getXRot()), 1.5f, (float) deltaTime);
            root.setRotX(newRootXRot);
            double yP = entity.wheelBase / (1f / Math.sin(Math.toRadians(Math.abs(entity.getXRot()))));
            root.setPosY((float) yP * 2);

            float newXBodyRot = MathUtils.fInterpToExp(body.getRotX(), (float) (entity.weightTransferX * entity.maxBodyPitch) * (Mth.PI/180), 1.5f, (float) deltaTime);
            float newZBodyRot = MathUtils.fInterpToExp(body.getRotZ(), (float) (entity.weightTransferZ * entity.maxBodyRoll) * (Mth.PI/180), 1.5f, (float) deltaTime);
            body.setRotX(newXBodyRot);
            body.setRotZ(newZBodyRot);
            entity.passengerXRot = (float) (Math.toDegrees(newXBodyRot) + Math.toDegrees(newRootXRot));
            entity.passengerZRot = newZBodyRot / (Mth.PI/180);
        }


    }*/

    protected void handleWheels(MolangParser parser, T vehicle, double deltaTime) {
        for (int i = 0; i < vehicle.Wheels.size(); i++) {
            WheelData wheel = vehicle.Wheels.get(i);
            wheel.setXRot(wheel.getXRot() + Math.toDegrees(wheel.angularVelocity * deltaTime));
            wheel.setYRot(MathUtils.dInterpToExp(wheel.getYRot(), (vehicle.steering * vehicle.steeringAngle), 4.25f, deltaTime));

            parser.setValue("query.wheel" + i + "_pitch", wheel::getXRot);
            if (wheel.affectedBySteering) parser.setMemoizedValue("query.wheel" + i + "_yaw", wheel::getYRot);
            parser.setValue("query.wheel" + i + "_y", () -> -wheel.springLength * 16);
        }
    }
    protected void handleEngine(MolangParser parser, T vehicle, double deltaTime) {
        parser.setMemoizedValue("query.is_engine_on", () -> RenderUtils.booleanToFloat(vehicle.isEngineOn()));
    }

    protected void handleBodyRotation(MolangParser parser, T vehicle, double deltaTime) {
        bodyPitch = MathUtils.fInterpToExp(bodyPitch, (float) (vehicle.weightTransferX * vehicle.maxBodyPitch), 1.5f, (float) deltaTime);
        bodyRoll = MathUtils.fInterpToExp(bodyRoll, (float) (vehicle.weightTransferZ * vehicle.maxBodyRoll), 1.5f, (float) deltaTime);
        parser.setValue("query.body_pitch", () -> bodyPitch);
        parser.setValue("query.body_roll", () -> bodyRoll);
    }
    protected void handleRootRotation(MolangParser parser, T vehicle, double deltaTime) {
        rootPitch = MathUtils.fInterpTo(rootPitch, (float) Math.toDegrees(vehicle.getXRot()), 1.5f, (float) deltaTime);
        parser.setValue("query.root_pitch", () -> rootPitch);
        parser.setValue("query.root_y", () -> vehicle.getTranslationOffset().y);
    }
    protected void handlePassengerRotation(MolangParser parser, T vehicle, double deltaTime) {
        vehicle.passengerXRot = bodyPitch + rootPitch;
        vehicle.passengerZRot = bodyRoll + rootRoll;
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return super.getRenderType(animatable, texture);
    }

    @Override
    public void applyMolangQueries(T vehicle, double animTime) {
        super.applyMolangQueries(vehicle, animTime);

        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
        lastFrame = new Date().getTime();

        handleWheels(parser, vehicle, deltaTime);
        handleEngine(parser, vehicle, deltaTime);
        handleBodyRotation(parser, vehicle, deltaTime);
        handleRootRotation(parser, vehicle, deltaTime);
        handlePassengerRotation(parser, vehicle, deltaTime);
        LOGGER.info(String.valueOf(vehicle.getId()));
    }
}

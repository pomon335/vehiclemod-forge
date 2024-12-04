package com.dawnestofbread.vehiclemod;

import com.dawnestofbread.vehiclemod.client.audio.AudioManager;
import com.dawnestofbread.vehiclemod.client.audio.SimpleEngineSound;
import com.dawnestofbread.vehiclemod.client.effects.SurfaceHelper;
import com.dawnestofbread.vehiclemod.utils.Curve;
import com.dawnestofbread.vehiclemod.utils.VectorUtils;
import com.dawnestofbread.vehiclemod.utils.WheelData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.*;

import static com.dawnestofbread.vehiclemod.client.audio.AudioManager.calculateVolume;
import static com.dawnestofbread.vehiclemod.utils.Maths.*;

public abstract class WheeledVehicle extends AbstractVehicle {
    public static final Logger LOGGER = VehicleMod.LOGGER;
    private static final EntityDataAccessor<Float> VELOCITY = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEIGHT_TRANSFER_X = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEIGHT_TRANSFER_Z = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RPM_DATA = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BRAKING = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.BOOLEAN);

    protected Vec3 forwardVelocity = new Vec3(0,0,0);
    protected Vec3 gravitationalAcceleration = new Vec3(0,-0.98,0);
    protected double angularVelocity;
    public WheelData[] Wheels;

    protected int driveWheelReferenceIndex;
    protected double weight, idleBrakeAmount = .1;
    protected double dragConstant = 0.4257, brakingConstant, corneringStiffness;

    protected double movementDirection;
    protected double idleRPM, shiftUpRPM, shiftDownRPM, maxRPM, engineForce;
    public Vec3 centreOfGeometry, frontAxle, rearAxle;

    protected Vec3 brakingForce, dragForce, centrifugalForce;
    protected double lateralForceFront, lateralForceRear;
    protected double forwardSpeed;
    protected Vec3 acceleration;
    protected Vec3[] exhaust;
    protected double exhaustFumeAmount;
    protected double driveWheelAngularVelocity;
    protected double engineTorque, driveTorque;
    protected boolean braking;
    protected double engineForceMultiplier;
    protected double slipRatio;
    protected double transmissionEfficiency; // .7 - .9
    protected Curve torqueCurve;
    protected int currentGear;
    protected int targetGear;
    private double shiftTimeLeft;
    protected double timeToShift;
    protected double steeringDelta, circleRadius, frontSlipAngle, rearSlipAngle, sideslipAngle;
    public double steeringAngle;
    public double maxBodyPitch; // How much the body rotates when shifting weight
    public double maxBodyRoll; // How much the body rotates when shifting weight
    public double weightTransferX, weightTransferZ; // 1 = full weight on front | -1 = full weight on rear

    protected double[] gearRatios;
    protected double differentialRatio;
    protected double traction;

    protected WheeledVehicle(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.setupWheels();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VELOCITY, 0f);
        this.entityData.define(WEIGHT_TRANSFER_X, 0f);
        this.entityData.define(WEIGHT_TRANSFER_Z, 0f);
        this.entityData.define(RPM_DATA, 0f);
        this.entityData.define(BRAKING, false);
    }

    protected void writeFloatTag(EntityDataAccessor<Float> dataAccessor, float in) {
        this.entityData.set(dataAccessor, in);
    }
    protected float readFloatTag(EntityDataAccessor<Float> dataAccessor) {
        return this.entityData.get(dataAccessor);
    }

    protected void writeBoolTag(EntityDataAccessor<Boolean> dataAccessor, boolean in) {
        this.entityData.set(dataAccessor, in);
    }
    protected boolean readBoolTag(EntityDataAccessor<Boolean> dataAccessor) {
        return this.entityData.get(dataAccessor);
    }


    @SubscribeEvent
    public void tick() {
        double deltaTime = (double) (new Date().getTime() - lastTick) / 1000;
        lastTick = new Date().getTime();
        super.tick(deltaTime);

        steering = fInterpTo(steering, steeringInput, 4.5f, (float) deltaTime);
        if (!this.level().isClientSide) {
            engineForce = throttle * engineForceMultiplier;

            forward = Vec3.directionFromRotation(this.getRotationVector());
            forwardSpeed = forwardVelocity.length();
            this.writeFloatTag(VELOCITY, (float) (forwardSpeed * movementDirection));

            double wheelBase = frontAxle.distanceTo(rearAxle);

            // Coming up next! A bit of complicated maths from 'Marco Monster', some random paper on vehicle physics, and my own stupidity!
            dragForce = new Vec3(-dragConstant * forwardVelocity.x * forwardSpeed, 0, -dragConstant * forwardVelocity.z * forwardSpeed);

            braking = (movementDirection > 0 && throttle < 0) || (movementDirection < 0 && throttle > 0) || throttle == 0;
            this.writeBoolTag(BRAKING, braking);
            brakingForce = new Vec3(0,0,-1).scale(throttle != 0 ? brakingConstant : brakingConstant * idleBrakeAmount).scale(handbrake == 1 ? 2f : 1f).scale(movementDirection);

            // The car now has hiccups while reversing? Never-mind, the reverse gear ratio had to be negative, which makes sense here
            if ((braking || throttle == 0 || handbrake == 1) && forwardSpeed > 0) acceleration = VectorUtils.divideVectorByScalar(brakingForce.add(dragForce), mass);
            else acceleration = VectorUtils.divideVectorByScalar(new Vec3(0,0,1).scale(driveTorque / Wheels[driveWheelReferenceIndex].radius).add(dragForce), mass);

            forwardVelocity = forwardVelocity.add(acceleration.scale(deltaTime));
            forwardSpeed = forwardVelocity.length();
            movementDirection = new Vec3(0,0,1).dot(forwardVelocity.normalize());

            weight = mass * gravity;

            driveWheelAngularVelocity = braking ? 0 : forwardSpeed / Wheels[driveWheelReferenceIndex].radius;

            RPM = Mth.clamp(Math.abs(driveWheelAngularVelocity * gearRatios[currentGear] * differentialRatio * 15) + idleRPM, idleRPM, maxRPM);
            this.writeFloatTag(RPM_DATA, (float) RPM);
            engineTorque = Math.abs(throttle) * torqueCurve.lookup(RPM);

            driveTorque = engineTorque * gearRatios[currentGear] * differentialRatio * transmissionEfficiency;

            slipRatio = ((driveWheelAngularVelocity * Wheels[driveWheelReferenceIndex].radius) - forwardSpeed) / forwardSpeed;
            slipRatio /= 1000;

            // Forward motion ends right about here
            // Onto steering now

            steeringDelta = steeringAngle * steering;
            circleRadius = (wheelBase / Math.sin(-steeringDelta * (Math.PI/180)));
            angularVelocity = forwardSpeed / circleRadius / ((Mth.PI/180) / 6);

            sideslipAngle = Math.atan(forwardVelocity.z / forwardVelocity.x);

            frontSlipAngle = Math.atan(((corneringStiffness * frontSlipAngle) + angularVelocity * frontAxle.distanceTo(centreOfGeometry)) / forwardSpeed) - steeringDelta * Math.signum(forwardSpeed);
            rearSlipAngle = Math.atan(((corneringStiffness * rearSlipAngle) - angularVelocity * rearAxle.distanceTo(centreOfGeometry)) / forwardSpeed);

            lateralForceFront = corneringStiffness * frontSlipAngle;
            lateralForceRear = corneringStiffness * rearSlipAngle;

            centrifugalForce = new Vec3(-1,0,0).scale(forwardSpeed / circleRadius);

            double centrifugalForceMultiplier = handbrake == 1 ? forwardSpeed / 2 : forwardSpeed / 5;
            centrifugalForceMultiplier *= 1.2 - traction;

            // Should be in a -1 — +1 range
            weightTransferX = (height / wheelBase) * ((acceleration.length() * (forward.dot(acceleration.yRot((float) Math.toRadians(-this.getYRot())).normalize()))) / gravity) * 2;
            // This equation is baloney, but the result sure does look nice
            weightTransferZ = (height / wheelBase) * ((centrifugalForce.length() * centrifugalForceMultiplier * (forward.yRot((float) Math.toRadians(90)).dot(centrifugalForce.yRot(-this.getYRot() * (Mth.PI/180)).normalize()))) / gravity) * 4;

            this.writeFloatTag(WEIGHT_TRANSFER_X, (float) weightTransferX);
            this.writeFloatTag(WEIGHT_TRANSFER_Z, (float) weightTransferZ);

            // Steering ends here
            // Onto actually using those calculations for something

            gearShift(deltaTime);
            updateWheels(deltaTime);

            // Makes turning 'balanced', you can't just keep gaining speed when turning really hard
            forwardVelocity = forwardVelocity.add(0,0,-centrifugalForce.length() * (forwardSpeed / 300));

            this.move(MoverType.SELF, forwardVelocity.yRot(-this.getYRot() * (Mth.PI/180)).add(centrifugalForce.scale(centrifugalForceMultiplier * 2).yRot(-this.getYRot() * (Mth.PI/180))).scale(deltaTime).add(this.onGround() ? Vec3.ZERO : gravitationalAcceleration));
            double yawVelocity = (float)((angularVelocity * deltaTime) * movementDirection);
            if (!Double.isNaN(yawVelocity)) this.turn(-yawVelocity, 0f);
        } else {
            forward = new Vec3(0,0,1).yRot(-this.getYRot() * (Mth.PI/180));
            //forwardVelocity = forward.multiply(this.getX() - this.xOld, this.getY() - this.yOld, this.getZ() - this.zOld);
            //forwardSpeed = forward.dot(forwardVelocity.normalize()) * forwardVelocity.length() / deltaTime;
            angularVelocity = Math.abs(this.getYRot() - this.yRotO);
            forwardSpeed = this.readFloatTag(VELOCITY);
            weightTransferX = this.readFloatTag(WEIGHT_TRANSFER_X);
            weightTransferZ = this.readFloatTag(WEIGHT_TRANSFER_Z);
            RPM = dInterpTo(RPM, this.readFloatTag(RPM_DATA), 3500f, deltaTime);
            braking = this.readBoolTag(BRAKING);
            updateWheelsClient(deltaTime);

            Map<AudioManager.SoundType, SimpleEngineSound> soundMap = SOUND_MANAGER.computeIfAbsent(this, v -> new EnumMap<>(AudioManager.SoundType.class));
            SimpleEngineSound idleSound = soundMap.get(AudioManager.SoundType.ENGINE_IDLE);
            SimpleEngineSound movingSound = soundMap.get(AudioManager.SoundType.ENGINE_MOVING);
            if (idleSound != null) idleSound.setVolume(calculateVolume(RPM, 0, idleRPM + 1000)).setPitch(mapDoubleRangeClamped(RPM, idleRPM, idleRPM + 1500, 1, 1.4));
            if (movingSound != null) movingSound.setVolume(mapDoubleRangeClamped(RPM, idleRPM, idleRPM + 1000, 0, 1)).setPitch(mapDoubleRangeClamped(RPM, idleRPM, maxRPM, .9, 1.4));

            for (Vec3 pos : exhaust) {
                for (int i = 0; i < Math.ceil(RPM/500); i++) {
                    Vec3 p = position().add(pos.xRot((float) this.getXRot()).yRot((float) Math.toRadians(-this.getYRot())).scale(.5));
                    Vec3 vel = forward.scale(movementDirection).add(0,-.05,0);

                    double probability = exhaustFumeAmount;
                    double result = Math.random();

                    if (result < probability)
                        Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, vel.x, vel.y, vel.z);
                }
            }
        }
    }

    protected void gearShift(double deltaTime) {
        if (shiftTimeLeft > 0) {
            currentGear = 1;
            shiftTimeLeft -= deltaTime;
        }
        if (shiftTimeLeft <= 0) {
            currentGear = targetGear;
            if (throttle > 0 && currentGear == 1) {currentGear = 2; targetGear = 2;}
            else if (throttle < 0 && currentGear == 1) {currentGear = 0; targetGear = 0;}
            else if (throttle > 0 && currentGear == 0) {currentGear = 1; targetGear = 1;}
            else if (RPM > shiftUpRPM && currentGear > 1 && throttle > 0 && targetGear + 1 < gearRatios.length) targetGear++;
            else if (RPM < shiftDownRPM && currentGear > 1 && throttle <= 0) targetGear--;
            if (currentGear != targetGear) shiftTimeLeft = timeToShift;
        }
    }
    protected abstract void setupWheels();

    protected final void updateWheels(double deltaTime) {
        for (int i = 0; i < Wheels.length; i++) {
            updateWheel(i, deltaTime);
        }
    }
    protected void updateWheel(int wheelIndex, double deltaTime) {
        if (!(Wheels.length > 0)) return;
        if (Wheels[wheelIndex] == null) return;
        WheelData wheel = Wheels[wheelIndex];

        wheel.currentRelativePosition = wheel.startingRelativePosition.yRot(-this.getYRot() * ((float)Math.PI / 180F));
        Vec3 offsetStart = this.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(-wheel.width / 2, -wheel.radius,-wheel.radius).yRot(-this.getYRot() * ((float)Math.PI / 180F)).xRot(-this.getXRot() * ((float)Math.PI / 180F))));
        Vec3 offsetEnd = this.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(wheel.width / 2, wheel.radius,wheel.radius).yRot(-this.getYRot() * ((float)Math.PI / 180F)).xRot(-this.getXRot() * ((float)Math.PI / 180F))));
        AABB aabb = new AABB(offsetStart.x , offsetStart.y, offsetStart.z, offsetEnd.x, offsetEnd.y, offsetEnd.z);
        List<VoxelShape> list = this.level().getEntityCollisions(this,aabb.expandTowards(0,-.1,0));
        wheel.onGround = collideBoundingBox(this, new Vec3(0,-1,0), aabb, this.level(), list).y == 0;
    }

    @OnlyIn(Dist.CLIENT)
    protected final void updateWheelsClient(double deltaTime) {
        for (int i = 0; i < Wheels.length; i++) {
            updateWheelClient(i, deltaTime);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateWheelClient(int wheelIndex, double deltaTime) {
        if (!(Wheels.length > 0)) return;
        if (Wheels[wheelIndex] == null) return;
        WheelData wheel = Wheels[wheelIndex];

        wheel.currentRelativePosition = wheel.startingRelativePosition.yRot(-this.getYRot() * ((float)Math.PI / 180F));
        wheel.onGround = this.onGround();

        if (wheel.affectedByEngine) {
            wheel.angularVelocity = forwardSpeed / wheel.radius;
        } else if (wheel.onGround) {
            wheel.angularVelocity = (forwardSpeed / wheel.radius);
            if (wheel.affectedBySteering) angularVelocity /= steering * steeringAngle;
        } else wheel.angularVelocity *= 0.8;
        if (((wheel.affectedByBrake && braking) || (wheel.affectedByHandbrake & handbrake > 0f))) wheel.angularVelocity = 0;

        double probability = Math.abs(wheel.angularVelocity) / 20;
        double result = Math.random();

        if (result < probability && wheel.affectedByEngine) SurfaceHelper.spawnFrictionEffect(SurfaceHelper.getSurfaceFromPosition(this.getBlockPosBelowThatAffectsMyMovement()), this.position().add(wheel.currentRelativePosition.x / 2, wheel.currentRelativePosition.y / 2, wheel.currentRelativePosition.z / 2), forward.scale(movementDirection).add(0,.01,0));
        if ((((wheel.affectedByBrake && braking) || (wheel.affectedByHandbrake & handbrake > 0f) || (!wheel.affectedBySteering && angularVelocity > 0.5 && forwardSpeed > 7)) && Math.abs(forwardSpeed) > 1)) SurfaceHelper.spawnSkidEffect(SurfaceHelper.getSurfaceFromPosition(this.getBlockPosBelowThatAffectsMyMovement()), this.position().add(wheel.currentRelativePosition.x / 2, wheel.currentRelativePosition.y / 2, wheel.currentRelativePosition.z / 2), forward.scale(movementDirection).add(0,.01,0));
    }

    protected void updatePassengerPosition(Entity passenger)
    {
        super.updatePassengerPosition(passenger);
        if (!this.level().isClientSide) {
            ServerPlayer castedEntity = (ServerPlayer) passenger;
            //                                                                                                 This should be multiplied by 3.6, but it's faked for gameplay’s sake
            // Display current speed and gear                                                                  Why lie to the player? Because I can!
            castedEntity.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(String.valueOf(Math.round(forwardSpeed * 4.3)) + "km/h \n" + "Gear: " + (currentGear == 0 ? "R" : currentGear == 1 ? "N" : String.valueOf(currentGear - 1)) + "\nRPM: " + String.valueOf(Math.round(RPM)) + "\nTorque: " + String.valueOf(Math.round(engineTorque)) + "\nSlip: " + String.valueOf(Math.round(slipRatio * 1000)) + "\nThrottle: " + String.valueOf(throttle) + "\n Steering (input over actual value) " + String.valueOf(steeringInput) + "/" + String.valueOf(steering)))); // Long boi
        }
    }
}

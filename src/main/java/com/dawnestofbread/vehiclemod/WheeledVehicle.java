package com.dawnestofbread.vehiclemod;

import com.dawnestofbread.vehiclemod.client.audio.AudioManager;
import com.dawnestofbread.vehiclemod.client.audio.SimpleEngineSound;
import com.dawnestofbread.vehiclemod.client.effects.SurfaceHelper;
import com.dawnestofbread.vehiclemod.utils.Curve;
import com.dawnestofbread.vehiclemod.utils.HitResult;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.dawnestofbread.vehiclemod.client.audio.AudioManager.calculateVolume;
import static com.dawnestofbread.vehiclemod.utils.LineTrace.lineTraceByType;
import static com.dawnestofbread.vehiclemod.utils.MathUtils.*;
import static com.dawnestofbread.vehiclemod.utils.VectorUtils.rotateVectorToEntitySpace;
import static com.dawnestofbread.vehiclemod.utils.VectorUtils.rotateVectorToEntitySpaceYOnly;

public abstract class WheeledVehicle extends AbstractVehicle {
    public static final Logger LOGGER = VehicleMod.LOGGER;
    private static final EntityDataAccessor<Float> VELOCITY = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEIGHT_TRANSFER_X = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WEIGHT_TRANSFER_Z = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RPM_DATA = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BRAKING = SynchedEntityData.defineId(WheeledVehicle.class, EntityDataSerializers.BOOLEAN);
    public List<WheelData> Wheels;
    public double wheelBase;
    public Vec3 centreOfGeometry, frontAxle, rearAxle;
    public double steeringAngle;
    public double maxBodyPitch; // How much the body rotates when shifting weight
    public double maxBodyRoll; // How much the body rotates when shifting weight
    public double weightTransferX, weightTransferZ; // 1 = full weight on front | -1 = full weight on rear
    public double climbAmount = .5;
    protected Vec3 acceleration;
    protected double angularAccelerationPitch;
    protected double angularVelocity;
    protected double angularVelocityPitch;
    protected double baseHeight;
    protected boolean braking;
    protected Vec3 brakingForce, dragForce, centrifugalForce;
    protected double centrifugalForceMultiplier;
    protected int currentGear;
    protected double differentialRatio;
    protected final double dragConstant = 0.4257;
    protected double brakingConstant;
    protected double corneringStiffness;
    protected double driveWheelAngularVelocity;
    protected int driveWheelReferenceIndex;
    protected double engineForceMultiplier;
    protected double engineTorque, driveTorque;
    protected Vec3[] exhaust;
    protected double exhaustFumeAmount;
    protected double forwardSpeed;
    protected Vec3 forwardVelocity = new Vec3(0, 0, 0);
    protected double[] gearRatios;
    protected final Vec3 gravitationalAcceleration = new Vec3(0, -0.0009, 0);
    protected double idleRPM, shiftUpRPM, shiftDownRPM, maxRPM, engineForce;
    protected double inertiaPitch;
    protected double lateralForceFront, lateralForceRear;
    protected double movementDirection;
    protected double slipRatio;
    protected double steeringDelta, circleRadius, frontSlipAngle, rearSlipAngle, sideslipAngle;
    protected int targetGear;
    protected double timeToShift;
    protected Curve torqueCurve;
    protected double torquePitch;
    protected double traction;
    protected double transmissionEfficiency; // .7 - .9
    protected double weight, idleBrakeAmount = .1;
    private double shiftTimeLeft;

    protected WheeledVehicle(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.setupWheels();
    }

    protected abstract void setupWheels();

    public double getMovementDirection() {
        return movementDirection;
    }

    public double getForwardSpeed() {
        return forwardSpeed;
    }

    @Override
    public float getStepHeight() {
        return .5f;
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

    protected void updatePassengerPosition(Entity passenger) {
        super.updatePassengerPosition(passenger);
        if (!this.level().isClientSide) {
            ServerPlayer castedEntity = (ServerPlayer) passenger;
            //                                                                                                 This should be multiplied by 3.6, but it's faked for gameplay’s sake
            // Display current speed and gear                                                                  Why lie to the player? Because I can!
            castedEntity.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(Math.round(forwardSpeed * 4.3) + "km/h \n" + "Gear: " + (currentGear == 0 ? "R" : currentGear == 1 ? "N" : String.valueOf(currentGear - 1)) + "\nRPM: " + Math.round(RPM) + "\nTorque: " + Math.round(engineTorque) + "\nSpring length: " + Wheels.get(0).springLength * 100 + "cm"))); // Long boi
        }
    }

    @SubscribeEvent
    public void tick() {
        double deltaTime = (double) (new Date().getTime() - lastTick) / 1000;
        lastTick = new Date().getTime();
        super.tick(deltaTime);

        steering = fInterpTo(steering, steeringInput, 4.5f, (float) deltaTime);
        if (!this.level().isClientSide) {
            simulateVehicleServer(deltaTime);
            simulateGravity(deltaTime);
            this.move(MoverType.SELF, forwardVelocity.yRot(-this.getYRot() * (Mth.PI / 180)).add(centrifugalForce.scale(centrifugalForceMultiplier * 2).yRot(-this.getYRot() * (Mth.PI / 180))).scale(deltaTime).add(this.onGround() ? Vec3.ZERO : gravitationalAcceleration.scale(mass)));
            double yawVelocity = (float) ((angularVelocity * deltaTime) * movementDirection);
            if (!Double.isNaN(yawVelocity)) this.turn(-yawVelocity, 0);
        } else {
            simulateVehicleClient(deltaTime);
        }
    }

    protected void simulateVehicleServer(double deltaTime) {
        WheelData driveWheel = Wheels.get(driveWheelReferenceIndex);
        engineForce = throttle * engineForceMultiplier;

        forward = new Vec3(0,0,1);
        forwardSpeed = forwardVelocity.length();
        this.writeFloatTag(VELOCITY, (float) (forwardSpeed * movementDirection));

        wheelBase = frontAxle.distanceTo(rearAxle);

        // Coming up next! A bit of complicated maths from 'Marco Monster', some random paper on vehicle physics, and my own stupidity!
        dragForce = new Vec3(-dragConstant * forwardVelocity.x * forwardSpeed, 0, -dragConstant * forwardVelocity.z * forwardSpeed);

        braking = (movementDirection > 0 && throttle < 0) || (movementDirection < 0 && throttle > 0) || throttle == 0 || handbrake > 0;
        this.writeBoolTag(BRAKING, braking);
        brakingForce = forward.reverse().scale(throttle != 0 ? brakingConstant : brakingConstant * idleBrakeAmount).scale(handbrake == 1 ? 2f : 1f).scale(movementDirection);

        if (braking) {
            acceleration = VectorUtils.divideVectorByScalar(brakingForce.add(dragForce), mass);
            if (acceleration.scale(deltaTime).length() > forwardSpeed) {
                acceleration = Vec3.ZERO;
                forwardVelocity = Vec3.ZERO;
            }
        }
        else acceleration = VectorUtils.divideVectorByScalar(new Vec3(0, 0, 1).scale(driveTorque / driveWheel.radius).add(dragForce), mass);

        forwardVelocity = forwardVelocity.add(acceleration.scale(deltaTime));
        forwardSpeed = forwardVelocity.length();
        movementDirection = forward.dot(forwardVelocity.normalize());

        weight = mass * gravity;

        driveWheelAngularVelocity = braking ? 0 : forwardSpeed / driveWheel.radius;

        RPM = Mth.clamp(Math.abs(driveWheelAngularVelocity * gearRatios[currentGear] * differentialRatio * 15) + idleRPM, idleRPM, maxRPM);
        this.writeFloatTag(RPM_DATA, (float) RPM);
        engineTorque = Math.abs(throttle) * torqueCurve.lookup(RPM);

        driveTorque = engineTorque * gearRatios[currentGear] * differentialRatio * transmissionEfficiency;

        slipRatio = ((driveWheelAngularVelocity * driveWheel.radius) - forwardSpeed) / forwardSpeed;
        slipRatio /= 1000;

        // Forward motion ends right about here
        // Onto steering now

        steeringDelta = steeringAngle * steering;
        circleRadius = (wheelBase / Math.sin(-steeringDelta * (Math.PI / 180)));
        angularVelocity = forwardSpeed / circleRadius / ((Mth.PI / 180) / 10);

        sideslipAngle = Math.atan(forwardVelocity.z / forwardVelocity.x);

        frontSlipAngle = Math.atan(((corneringStiffness * frontSlipAngle) + angularVelocity * frontAxle.distanceTo(centreOfGeometry)) / forwardSpeed) - steeringDelta * Math.signum(forwardSpeed);
        rearSlipAngle = Math.atan(((corneringStiffness * rearSlipAngle) - angularVelocity * rearAxle.distanceTo(centreOfGeometry)) / forwardSpeed);

        lateralForceFront = corneringStiffness * frontSlipAngle;
        lateralForceRear = corneringStiffness * rearSlipAngle;

        centrifugalForce = new Vec3(-1, 0, 0).scale(forwardSpeed / circleRadius);

        centrifugalForceMultiplier = handbrake == 1 ? forwardSpeed / 2 : forwardSpeed / 5;
        centrifugalForceMultiplier *= 1.2 - traction;

        // Should be in a -1 — +1 range
        weightTransferX = (height / wheelBase) * ((acceleration.length() * (forward.dot(acceleration.yRot((float) Math.toRadians(-this.getYRot())).normalize()))) / gravity) * 2;
        // This equation is baloney, but the result sure does look nice
        weightTransferZ = (height / wheelBase) * ((centrifugalForce.length() * centrifugalForceMultiplier * (forward.yRot((float) Math.toRadians(90)).dot(centrifugalForce.yRot(-this.getYRot() * (Mth.PI / 180)).normalize()))) / gravity) * 4;

        this.writeFloatTag(WEIGHT_TRANSFER_X, (float) weightTransferX);
        this.writeFloatTag(WEIGHT_TRANSFER_Z, (float) weightTransferZ);

        // Steering ends here
        // Onto actually using those calculations for something

        gearShift(deltaTime);
        updateWheelsServer(deltaTime);


        // Makes turning 'balanced', you can't just keep gaining speed when turning really hard
        forwardVelocity = forwardVelocity.add(0, 0, -centrifugalForce.length() * (forwardSpeed / 300));
    }

    protected void gearShift(double deltaTime) {
        if (shiftTimeLeft > 0) {
            currentGear = 1;
            shiftTimeLeft -= deltaTime;
        }
        if (shiftTimeLeft <= 0) {
            currentGear = targetGear;
            if (throttle > 0 && currentGear == 1) {
                currentGear = 2;
                targetGear = 2;
            } else if (throttle < 0 && currentGear == 1) {
                currentGear = 0;
                targetGear = 0;
            } else if (throttle > 0 && currentGear == 0) {
                currentGear = 1;
                targetGear = 1;
            } else if (RPM > shiftUpRPM && currentGear > 1 && throttle > 0 && targetGear + 1 < gearRatios.length)
                targetGear++;
            else if (RPM < shiftDownRPM && currentGear > 1 && throttle <= 0) targetGear--;
            if (currentGear != targetGear) shiftTimeLeft = timeToShift;
        }
    }

    protected final void updateWheelsServer(double deltaTime) {
        for (int i = 0; i < Wheels.size(); i++) {
            updateWheelServer(i, deltaTime);
        }
    }

    protected void updateWheelServer(int wheelIndex, double deltaTime) {
        if (!(Wheels.size() > 0)) return;
        if (Wheels.get(wheelIndex) == null) return;
        WheelData wheel = Wheels.get(wheelIndex);

        HitResult wheelTrace = checkWheelOnGroundRaycast(wheel);
        wheel.onGround = wheelTrace.hit;
        wheel.springLength = wheelTrace.hit ? wheelTrace.distance - wheel.springMinLength - wheel.radius : wheel.springMaxLength;
        wheel.currentRelativePosition = rotateVectorToEntitySpace(wheel.startingRelativePosition.scale(.5).add(0, -wheel.springLength, 0), this);
    }

    private HitResult checkWheelOnGroundRaycast(WheelData wheel) {
        Vec3 offsetStart = this.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(-wheel.width / 2, -wheel.radius, -wheel.radius).yRot(-this.getYRot() * ((float) Math.PI / 180F)).xRot(-this.getXRot() * ((float) Math.PI / 180F))));
        Vec3 offsetEnd = this.position().add(wheel.currentRelativePosition.scale(0.5).add(new Vec3(wheel.width / 2, wheel.radius, wheel.radius).yRot(-this.getYRot() * ((float) Math.PI / 180F)).xRot(-this.getXRot() * ((float) Math.PI / 180F))));

        Vec3 lineTraceStart = this.position().add(rotateVectorToEntitySpace(wheel.startingRelativePosition.scale(.5).add(0, wheel.springMinLength, 0), this));
        Vec3 lineTraceEnd = this.position().add(rotateVectorToEntitySpace(wheel.startingRelativePosition.scale(.5).add(0, -wheel.springMaxLength, 0), this));

        AABB aabb = new AABB(offsetStart.x, offsetStart.y, offsetStart.z, offsetEnd.x, offsetEnd.y, offsetEnd.z);

        return lineTraceByType(lineTraceStart, lineTraceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this);
        //return isCollidingWithBlocks(new BlockPos((int) Mth.floor(offsetCentre.x), (int) Mth.floor(offsetCentre.y), (int) Mth.floor(offsetCentre.z)).below(), aabb.expandTowards(0,-1,0));
    }

    protected void simulateGravity(double deltaTime) {
    }

    protected void simulateVehicleClient(double deltaTime) {
        forward = new Vec3(0, 0, 1).yRot(-this.getYRot() * (Mth.PI / 180));
        angularVelocity = Math.abs(this.getYRot() - this.yRotO);
        forwardSpeed = this.readFloatTag(VELOCITY);
        movementDirection = (forwardSpeed > 0 ? 1f : forwardSpeed < 0 ? -1f : 0f);
        weightTransferX = this.readFloatTag(WEIGHT_TRANSFER_X);
        weightTransferZ = this.readFloatTag(WEIGHT_TRANSFER_Z);
        RPM = dInterpTo(RPM, this.readFloatTag(RPM_DATA), 3500f, deltaTime);
        braking = this.readBoolTag(BRAKING);
        updateWheelsClient(deltaTime);
        updateVehicleRotation(deltaTime);
        wheelBase = frontAxle.distanceTo(rearAxle);

        Map<AudioManager.SoundType, SimpleEngineSound> soundMap = SOUND_MANAGER.computeIfAbsent(this, v -> new EnumMap<>(AudioManager.SoundType.class));
        SimpleEngineSound idleSound = soundMap.get(AudioManager.SoundType.ENGINE_IDLE);
        SimpleEngineSound movingSound = soundMap.get(AudioManager.SoundType.ENGINE_MOVING);
        if (idleSound != null)
            idleSound.setVolume(calculateVolume(RPM, 0, idleRPM + 1000)).setPitch(mapDoubleRangeClamped(RPM, idleRPM, idleRPM + 1500, 1, 1.4));
        if (movingSound != null)
            movingSound.setVolume(mapDoubleRangeClamped(RPM, idleRPM, idleRPM + 1000, 0, 1)).setPitch(mapDoubleRangeClamped(RPM, idleRPM, maxRPM, .9, 1.4));

        if (isEngineOn()) {
            for (Vec3 pos : exhaust) {
                for (int i = 0; i < Math.ceil(RPM / 250); i++) {
                    Vec3 p = position().add(pos.xRot(this.getXRot()).yRot((float) Math.toRadians(-this.getYRot())).scale(.5));
                    Vec3 vel = forward.scale(-movementDirection / 8).add(0, -.05, 0);

                    double probability = exhaustFumeAmount;
                    double result = Math.random();

                    if (result < probability)
                        Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, vel.x, vel.y, vel.z);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected final void updateWheelsClient(double deltaTime) {
        for (int i = 0; i < Wheels.size(); i++) {
            updateWheelClient(i, deltaTime);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateWheelClient(int wheelIndex, double deltaTime) {
        if (!(Wheels.size() > 0)) return;
        if (Wheels.get(wheelIndex) == null) return;
        WheelData wheel = Wheels.get(wheelIndex);

        HitResult wheelTrace = checkWheelOnGroundRaycast(wheel);
        wheel.onGround = wheelTrace.hit;
        wheel.springLength = wheelTrace.hit ? wheelTrace.distance - wheel.springMinLength - wheel.radius : wheel.springMaxLength;
        wheel.currentRelativePosition = wheel.startingRelativePosition.scale(.5).add(0, -wheel.springLength, 0).yRot(-this.getYRot() * ((float) Math.PI / 180F));

        if (wheel.affectedByEngine) {
            wheel.angularVelocity = forwardSpeed / wheel.radius;
        } else if (wheel.onGround) {
            wheel.angularVelocity = (forwardSpeed / wheel.radius);
            if (wheel.affectedBySteering) angularVelocity /= steering * steeringAngle;
        } else wheel.angularVelocity *= 0.8;
        if (((wheel.affectedByBrake && braking) || (wheel.affectedByHandbrake & handbrake > 0f)))
            wheel.angularVelocity = 0;

        double probability = Math.abs(wheel.angularVelocity) / 20;
        double result = Math.random();

        HitResult climbTrace = checkWheelShouldClimbRaycast(wheel);
        if (climbTrace.hit && !climbTrace.inside) {
            wheel.targetWorldPosition = climbTrace.end.add(0, wheel.radius, 0).add(rotateVectorToEntitySpaceYOnly(new Vec3(0, 0, Math.sqrt(Math.pow(climbTrace.start.x - climbTrace.end.x, 2) + Math.pow(climbTrace.start.z - climbTrace.end.z, 2)) * -movementDirection), this));
        } else {
            wheel.targetWorldPosition = wheel.currentRelativePosition.add(this.position());
        }

        if (result < probability && wheel.affectedByEngine)
            SurfaceHelper.spawnFrictionEffect(SurfaceHelper.getSurfaceFromPosition(this.getBlockPosBelowThatAffectsMyMovement()), this.position().add(wheel.currentRelativePosition.x / 2, wheel.currentRelativePosition.y / 2, wheel.currentRelativePosition.z / 2), forward.scale(-movementDirection).add(0, .01, 0));
        if ((((wheel.affectedByBrake && braking) || (wheel.affectedByHandbrake & handbrake > 0f) || (!wheel.affectedBySteering && angularVelocity > 0.5 && forwardSpeed > 7)) && Math.abs(forwardSpeed) > 1))
            SurfaceHelper.spawnSkidEffect(SurfaceHelper.getSurfaceFromPosition(this.getBlockPosBelowThatAffectsMyMovement()), this.position().add(wheel.currentRelativePosition.x / 2, wheel.currentRelativePosition.y / 2, wheel.currentRelativePosition.z / 2), forward.scale(-movementDirection / 16).add(0, .01, 0));
    }

    private HitResult checkWheelShouldClimbRaycast(WheelData wheel) {
        Vec3 lineTraceStart = this.position().add(rotateVectorToEntitySpaceYOnly(wheel.startingRelativePosition.scale(.5).add(0, climbAmount * 1.01, 0), this));
        Vec3 lineTraceEnd = lineTraceStart.add(0, -wheelBase - wheel.radius, 0).add(rotateVectorToEntitySpaceYOnly(new Vec3(0, 0, wheel.radius * 1.5 * movementDirection), this));

        return lineTraceByType(lineTraceStart, lineTraceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this);
    }

    private void updateVehicleRotation(double deltaTime) {
        int wheelCount = Wheels.size();
        List<WheelData> frontWheels = Wheels.subList(0, wheelCount / 2); // Front wheels (first half)
        List<WheelData> rearWheels = Wheels.subList(wheelCount / 2, wheelCount); // Rear wheels (second half)

        Vec3 frontMidpoint = calculateAxleMidpoint(frontWheels);
        Vec3 rearMidpoint = calculateAxleMidpoint(rearWheels);

        Vec3 frontLeftMostWheel = getLeftMostWheelPosition(frontWheels);
        Vec3 frontRightMostWheel = getRightMostWheelPosition(rearWheels);

        Vec3 rearLeftMostWheel = getLeftMostWheelPosition(frontWheels);
        Vec3 rearRightMostWheel = getRightMostWheelPosition(rearWheels);

        double horizontalDistance = Math.sqrt(Math.pow(frontMidpoint.x - rearMidpoint.x, 2) + Math.pow(frontMidpoint.z - rearMidpoint.z, 2));
        double inclineAngle = Math.atan2(frontMidpoint.y - rearMidpoint.y, horizontalDistance);
        double inclineAngleDegrees = Math.toDegrees(inclineAngle);

        // Create alignment plane
        Vec3 v1 = frontRightMostWheel.subtract(rearLeftMostWheel);
        Vec3 v2 = frontLeftMostWheel.subtract(rearRightMostWheel);
        Vec3 normal = v1.cross(v2).normalize();

        double pitch = Math.asin(normal.y());
        double roll = Math.atan2(normal.x(), normal.z());

        double minY = Math.min(Math.min(frontLeftMostWheel.y, frontRightMostWheel.y), Math.min(rearLeftMostWheel.y, rearRightMostWheel.y));
        double vehicleY = minY + baseHeight;

        setXRot((float) pitch);
        setTranslationOffset(0, vehicleY, 0);
        //setPos(this.getX(), this.getY() + yDifference,this.getZ());
    }

    public Vec3 calculateAxleMidpoint(List<WheelData> wheels) {
        Vec3 midpoint = Vec3.ZERO;
        for (WheelData wheel : wheels) {
            midpoint = midpoint.add(wheel.targetWorldPosition);
        }
        return midpoint.scale(1.0 / wheels.size());
    }

    private Vec3 getLeftMostWheelPosition(List<WheelData> wheels) {
        WheelData leftmostWheel = wheels.get(0);
        for (WheelData wheel : wheels) {
            if (wheel.startingRelativePosition.x() < leftmostWheel.startingRelativePosition.x()) {
                leftmostWheel = wheel;
            }
        }
        return leftmostWheel.targetWorldPosition;
    }

    private Vec3 getRightMostWheelPosition(List<WheelData> wheels) {
        WheelData rightmostWheel = wheels.get(0);
        for (WheelData wheel : wheels) {
            if (wheel.startingRelativePosition.x() > rightmostWheel.startingRelativePosition.x()) {
                rightmostWheel = wheel;
            }
        }
        return rightmostWheel.targetWorldPosition;
    }

    @Override
    public boolean onGround() {
        return super.onGround();
    }
}

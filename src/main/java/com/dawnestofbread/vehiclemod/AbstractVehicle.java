package com.dawnestofbread.vehiclemod;

import com.dawnestofbread.vehiclemod.client.audio.AudioManager;
import com.dawnestofbread.vehiclemod.client.audio.SimpleEngineSound;
import com.dawnestofbread.vehiclemod.network.*;
import com.dawnestofbread.vehiclemod.utils.MathUtils;
import com.dawnestofbread.vehiclemod.utils.SeatData;
import com.eliotlash.mclib.math.functions.classic.Abs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static com.dawnestofbread.vehiclemod.client.audio.AudioManager.*;

public abstract class AbstractVehicle extends Entity implements GeoEntity {
    public static final Logger LOGGER = VehicleMod.LOGGER;
    protected static final EntityDataAccessor<Float> THROTTLE = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> STEERING = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<CompoundTag> SEAT_MANAGER = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.COMPOUND_TAG);
    protected final WeakHashMap<AbstractVehicle, EnumMap<AudioManager.SoundType, SimpleEngineSound>> SOUND_MANAGER = new WeakHashMap<>();
    public List<UUID> SeatManager;
    public SeatData[] Seats;
    public float passengerXRot;
    public float passengerZRot;
    public double width;
    public double length;
    public double height;
    public Map<AudioManager.SoundType, SoundEvent> engineSounds;
    public float throttle = 0;
    public float steeringInput = 0;
    public float steering = 0;
    public float handbrake = 0;
    public float sprint = 0;
    protected double RPM = 0;
    protected AABB[] collision;
    protected Vec3 forward;
    protected final double gravity = 9.81;
    protected long lastTick;
    protected int lerpSteps;
    protected double lerpX;
    protected double lerpXRot;
    protected double lerpY;
    protected double lerpYRot;
    protected double lerpZ;
    protected double mass = 1000;
    boolean inputForward = false;
    boolean inputBackward = false;
    boolean inputRight = false;
    boolean inputLeft = false;
    boolean inputJump = false;
    boolean inputSprint = false;
    private Vec3 translationOffset = new Vec3(0, 0, 0);
    private float zRot;

    public boolean isEngineOn() {
        return !SeatManager.get(0).equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    protected AbstractVehicle(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.noPhysics = false;
        this.ejectPassengers();
        this.setupSeats();
    }

    protected abstract void setupSeats();

    public Vec3 getTranslationOffset() {
        return translationOffset;
    }

    public void setTranslationOffset(Vec3 translationOffset) {
        this.translationOffset = translationOffset;
    }

    public void setTranslationOffset(double x, double y, double z) {
        this.translationOffset = new Vec3(x, y, z);
    }

    protected final void writeFloatTag(EntityDataAccessor<Float> dataAccessor, float in) {
        this.entityData.set(dataAccessor, in);
    }

    protected final void writeBoolTag(EntityDataAccessor<Boolean> dataAccessor, boolean in) {
        this.entityData.set(dataAccessor, in);
    }

    protected float readFloatTag(EntityDataAccessor<Float> dataAccessor) {
        return this.entityData.get(dataAccessor);
    }

    protected boolean readBoolTag(EntityDataAccessor<Boolean> dataAccessor) {
        return this.entityData.get(dataAccessor);
    }

    public float getZRot() {
        return zRot;
    }

    public void setZRot(float zRot) {
        this.zRot = zRot;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SEAT_MANAGER, new CompoundTag());
        this.entityData.define(THROTTLE, 0F);
        this.entityData.define(STEERING, 0F);
    }

    @Override
    protected final void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    // Come on you can't just push a vehicle, you're not *that* strong
    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("SeatManager", Tag.TAG_COMPOUND)) readSeatManager(compound.getCompound("SeatManager"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("SeatManager", writeSeatManager());
    }

    protected CompoundTag writeSeatManager() {
        CompoundTag SeatManagerTag = new CompoundTag();
        ListTag SeatManagerList = new ListTag();
        for (int i = 0; i < SeatManager.size(); i++) {
            CompoundTag seatTag = new CompoundTag();
            seatTag.putUUID("UUID", SeatManager.get(i));
            seatTag.putInt("Index", i);
            SeatManagerList.add(seatTag);
        }
        SeatManagerTag.put("seatList", SeatManagerList);
        return SeatManagerTag;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.level().isClientSide && !player.isCrouching()) {
            // Unused for now
            ItemStack heldItem = player.getItemInHand(hand);

            if (this.canRide(player)) {
                if (SeatManager.contains(player.getUUID())) return InteractionResult.SUCCESS;
                int closestSeatIndex = -1;
                double closestDistance = 0;
                for (int i = 0; i < Seats.length; i++) {
                    SeatData seat = Seats[i];
                    if (!SeatManager.get(i).equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) continue;

                    Vec3 seatVec = seat.seatOffset.yRot(-this.getYRot() * ((float) Math.PI / 180F)).add(this.position());
                    double distance = player.distanceToSqr(seatVec.x, seatVec.y - player.getBbHeight() / 2F, seatVec.z);
                    if (closestSeatIndex == -1 || distance < closestDistance) {
                        closestSeatIndex = i;
                        closestDistance = distance;
                    }
                }
                LOGGER.info("Seat info: " + closestSeatIndex + " - " + player.getName() + " - " + (player.level().isClientSide ? "Client" : "Server"));
                LOGGER.info("Chosen 'closest' seat: " + closestSeatIndex + " with distance of " + closestDistance + " metres");
                SeatManager.set(closestSeatIndex, player.getUUID());
                this.entityData.set(SEAT_MANAGER, writeSeatManager(), true);
                if (closestSeatIndex != -1) player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        //LOGGER.info(SeatManager.toString() + " - " + (player.level().isClientSide ? "Client" : "Server"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void positionRider(Entity rider_, MoveFunction moveFunc) {
        super.positionRider(rider_, moveFunc);
        updatePassengerPosition(rider_);
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return SeatManager.contains(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    @Override
    public void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.setPos(this.lerpX, this.lerpY, this.lerpZ);
            this.setYRot((float) this.lerpYRot);
            this.setXRot((float) this.lerpXRot);
        }
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (!entity.level().isClientSide && SeatManager.contains(entity.getUUID())) {
            if (SeatManager.indexOf(entity.getUUID()) == 0) throttle = 0;
            SeatManager.set(SeatManager.indexOf(entity.getUUID()), UUID.fromString("00000000-0000-0000-0000-000000000000"));
            this.entityData.set(SEAT_MANAGER, writeSeatManager());
        }
        LOGGER.info(SeatManager.toString() + " - " + (this.level().isClientSide ? "Client" : "Server"));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < Seats.length;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yaw;
        this.lerpXRot = pitch;
        this.lerpSteps = posRotationIncrements;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (dataAccessor.equals(SEAT_MANAGER)) readSeatManager(this.entityData.get(SEAT_MANAGER));
    }

    protected void updatePassengerPosition(Entity passenger) {
        if (passenger.level().isClientSide() && this.hasPassenger(passenger)) {
            if (!SeatManager.contains(passenger.getUUID())) return;
            SeatData seat = Seats[SeatManager.indexOf(passenger.getUUID())];

            if (seat == null) return;
            passenger.setYBodyRot(this.getYRot() + seat.yawOffset);
            Vec3 position = new Vec3(this.position().x, this.position().y, this.position().z).add(seat.seatOffset.zRot((float) Math.toRadians(passengerZRot)).yRot(-this.getYRot() * ((float) Math.PI / 180F)));
            //position = position.yRot((this.getYRot()));
            passenger.setPos(position);
        }
    }

    protected void readSeatManager(CompoundTag tag) {
        if (!tag.contains("seatList")) return;
        ListTag SeatManagerList = tag.getList("seatList", Tag.TAG_COMPOUND);
        for (Tag value : SeatManagerList) {
            CompoundTag seatTag = (CompoundTag) value;
            SeatManager.set(seatTag.getInt("Index"), seatTag.getUUID("UUID"));
        }
    }

    @SubscribeEvent
    public void tick(double deltaTime) {
        super.tick();
        this.lerpTick();

        if (this.level().isClientSide) {
            if (this.SeatManager.get(0).equals(Minecraft.getInstance().player.getUUID())) {
                inputForward = Minecraft.getInstance().options.keyUp.isDown();
                inputBackward = Minecraft.getInstance().options.keyDown.isDown();
                inputRight = Minecraft.getInstance().options.keyRight.isDown();
                inputLeft = Minecraft.getInstance().options.keyLeft.isDown();

                inputJump = Minecraft.getInstance().options.keyJump.isDown();
                inputSprint = Minecraft.getInstance().options.keySprint.isDown();

                if ((inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f) != throttle)
                    PacketHandler.INSTANCE.sendToServer(new MessageThrottle(inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f));
                if ((inputLeft ? -1f : inputRight ? 1f : 0f) != steeringInput)
                    PacketHandler.INSTANCE.sendToServer(new MessageSteering(inputLeft ? -1f : inputRight ? 1f : 0f));
                if ((inputJump ? 1f : 0f) != handbrake)
                    PacketHandler.INSTANCE.sendToServer(new MessageHandbrake(inputJump ? 1f : 0f));
                if ((inputSprint ? 1f : 0f) != sprint)
                    PacketHandler.INSTANCE.sendToServer(new MessageSprint(inputSprint ? 1f : 0f));
                this.setThrottle(inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f);
                this.setSteering(inputLeft ? -1f : inputRight ? 1f : 0f);
                this.setHandbrake(inputJump ? 1f : 0f);
                this.setSprint(inputSprint ? 1f : 0f);

//                UpdateCamera(deltaTime);

                this.readSeatManager(this.entityData.get(SEAT_MANAGER));
                if (engineSounds.containsKey(AudioManager.SoundType.ENGINE_IDLE) && engineSounds.containsKey(AudioManager.SoundType.ENGINE_MOVING) && !SeatManager.get(0).equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                    playEngineSound(SOUND_MANAGER, this, AudioManager.SoundType.ENGINE_IDLE, this.engineSounds.get(AudioManager.SoundType.ENGINE_IDLE)).setVolume(0f);
                    playEngineSound(SOUND_MANAGER, this, AudioManager.SoundType.ENGINE_MOVING, this.engineSounds.get(AudioManager.SoundType.ENGINE_MOVING)).setVolume(0f);
                }
            }
        }
    }

    public void setThrottle(float power) {
        this.throttle = power;
    }

    public void setSprint(float input) {
        this.sprint = input;
    }

    public void setSteering(float input) {
        this.steeringInput = input;
    }

    public void setHandbrake(float input) {
        this.handbrake = input;
    }

    private void lerpTick() {
        if (this.lerpSteps > 0) {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double) this.lerpSteps;
            double d2 = this.getY() + (this.lerpY - this.getY()) / (double) this.lerpSteps;
            double d4 = this.getZ() + (this.lerpZ - this.getZ()) / (double) this.lerpSteps;
            double d6 = Mth.wrapDegrees(this.lerpYRot - (double) this.getYRot());
            double d8 = Mth.wrapDegrees(this.lerpXRot - (double) this.getXRot());
            float yRot = this.getYRot() + (float) d6 / (float) this.lerpSteps;
            float xRot = this.getXRot() + (float) d8 / (float) this.lerpSteps;
            --this.lerpSteps;
            this.setPos(d0, d2, d4);
            if (!Float.isNaN(xRot)) this.setXRot(xRot);
            if (!Float.isNaN(yRot)) this.setYRot(yRot);
        }
    }

    protected void UpdateCamera(double deltaTime) {
        Entity camera = Minecraft.getInstance().cameraEntity;

        float xPos = MathUtils.fInterpToExp((float) camera.getX(), (float) this.getX(), 3f, (float) deltaTime);
        float yPos = MathUtils.fInterpToExp((float) camera.getY(), (float) this.getY(), 3f, (float) deltaTime);
        float zPos = MathUtils.fInterpToExp((float) camera.getZ(), (float) this.getZ(), 3f, (float) deltaTime);

        camera.setPos(xPos, yPos, zPos);
        camera.setYRot(MathUtils.fInterpToExp(camera.getYRot(), this.getYRot(), 3f, (float) deltaTime));
    }

    protected boolean isCollidingWithBlocks(BlockPos pos, AABB aabb) {
        return aabb.intersects(new AABB(pos)) && !getBlockAtPos(pos).isAir();
    }

    protected BlockState getBlockAtPos(BlockPos pos) {
        return this.level().getBlockState(pos);
    }

    // Overriding the move method, because 'collide' is private
    // 99% of this is unchanged, except for the 'this.collide()' call

    private Vec3 doCollide(Vec3 motion) {
        double d1 = motion.x;
        double d2 = motion.y;
        double d3 = motion.z;
        for (AABB aabb : collision) {
            Vec3 checkedMotion = doCollisionCheckForAABB(aabb, motion);
            if (checkedMotion.x == 0) d1 = 0;
            if (checkedMotion.y == 0) d2 = 0;
            if (checkedMotion.z == 0) d3 = 0;
        }
        return new Vec3(d1 == 0 ? 0 : motion.x, d2 == 0 ? 0 : motion.y, d3 == 0 ? 0 : motion.z);
    }


    private Vec3 doCollisionCheckForAABB(AABB aabb, Vec3 motion) {
        List<VoxelShape> list = this.level().getEntityCollisions(this, aabb.expandTowards(motion));
        Vec3 vec3 = motion.lengthSqr() == 0.0D ? motion : collideBoundingBox(this, motion, aabb, this.level(), list);
        boolean flag = motion.x != vec3.x;
        boolean flag1 = motion.y != vec3.y;
        boolean flag2 = motion.z != vec3.z;
        boolean flag3 = this.onGround() || flag1 && motion.y < 0.0D;
        float stepHeight = getStepHeight();
        if (stepHeight > 0.0F && flag3 && (flag || flag2)) {
            Vec3 vec31 = collideBoundingBox(this, new Vec3(motion.x, stepHeight, motion.z), aabb, this.level(), list);
            Vec3 vec32 = collideBoundingBox(this, new Vec3(0.0D, stepHeight, 0.0D), aabb.expandTowards(motion.x, 0.0D, motion.z), this.level(), list);
            if (vec32.y < (double) stepHeight) {
                Vec3 vec33 = collideBoundingBox(this, new Vec3(motion.x, 0.0D, motion.z), aabb.move(vec32), this.level(), list).add(vec32);
                if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
                    vec31 = vec33;
                }
            }

            if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
                return vec31.add(collideBoundingBox(this, new Vec3(0.0D, -vec31.y + motion.y, 0.0D), aabb.move(vec31), this.level(), list));
            }
        }

        return vec3;
    }
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Flying", 0, this::flyAnimController));
    }

    protected <E extends AbstractVehicle> PlayState flyAnimController(final AnimationState<E> event) {
        return event.setAndContinue(RawAnimation.begin().thenLoop("programmable"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

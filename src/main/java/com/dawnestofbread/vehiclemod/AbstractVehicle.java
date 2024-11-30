package com.dawnestofbread.vehiclemod;

import com.dawnestofbread.vehiclemod.utils.Maths;
import com.eliotlash.mclib.utils.MathHelper;
import com.dawnestofbread.vehiclemod.network.MessageHandbrake;
import com.dawnestofbread.vehiclemod.network.MessageSteering;
import com.dawnestofbread.vehiclemod.network.MessageThrottle;
import com.dawnestofbread.vehiclemod.network.PacketHandler;
import com.dawnestofbread.vehiclemod.utils.SeatData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public abstract class AbstractVehicle extends Entity implements GeoEntity {
    public static final Logger LOGGER = VehicleMod.LOGGER;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation ANIM = RawAnimation.begin().thenLoop("programmable");

    public Vec3 translateOffset = new Vec3(0,0,0);
    public List<UUID> SeatManager;
    private static final EntityDataAccessor<CompoundTag> SEAT_TRACKER = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.COMPOUND_TAG);
    protected static final EntityDataAccessor<Float> THROTTLE = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> STEERING = SynchedEntityData.defineId(AbstractVehicle.class, EntityDataSerializers.FLOAT);
    public SeatData[] Seats;

    public float passengerXAdditional; // The current, additional, x rotation (e.g. a car's body pitch) !IN DEGREES!
    public float passengerZAdditional; // The current, additional, z rotation (e.g. a car's body pitch) !IN DEGREES!
    protected double mass = 1000;
    protected double gravity = 9.81;
    protected Vec3[][] collision;

    protected Vec3 forward;

    protected double RPM;

    boolean inputForward = false;
    boolean inputBackward = false;
    boolean inputRight = false;
    boolean inputLeft = false;
    boolean inputJump = false;
    public float throttle = 0;
    public float steeringInput = 0;
    public float steering = 0;
    public float handbrake = 0;
    private float nextStep = 1f;

    protected AbstractVehicle(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.noPhysics = false;
        this.ejectPassengers();
        this.setupSeats();
    }

    public void setThrottle(float power)
    {
        this.throttle = power;
    }
    public void setSteering(float input)
    {
        this.steeringInput = input;
    }
    public void setHandbrake(float input)
    {
        this.handbrake = input;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SEAT_TRACKER, new CompoundTag());
        this.entityData.define(THROTTLE, 0F);
        this.entityData.define(STEERING, 0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (dataAccessor.equals(SEAT_TRACKER)) readSeatManager(this.entityData.get(SEAT_TRACKER));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if (compound.contains("SeatManager", Tag.TAG_COMPOUND)) readSeatManager(compound.getCompound("SeatManager"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.put("SeatManager", writeSeatManager());
    }

    protected CompoundTag writeSeatManager() {
        CompoundTag SeatManagerInitial = new CompoundTag();
        ListTag SeatManagerList = new ListTag();
        for (int i = 0; i < SeatManager.size(); i++) {
            CompoundTag seatTag = new CompoundTag();
            seatTag.putUUID("UUID", SeatManager.get(i));
            seatTag.putInt("Index", i);
            SeatManagerList.add(seatTag);
        };
        SeatManagerInitial.put("seatList",SeatManagerList);
        return SeatManagerInitial;
    }

    protected void readSeatManager(CompoundTag tag) {
        if (!tag.contains("seatList")) return;
        ListTag SeatManagerList = tag.getList("seatList", Tag.TAG_COMPOUND);
        for (Tag value : SeatManagerList) {
            CompoundTag seatTag = (CompoundTag) value;
            SeatManager.set(seatTag.getInt("Index"), seatTag.getUUID("UUID"));
        };
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected final void playStepSound(BlockPos blockPos, BlockState blockState) {}

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = (double) yaw;
        this.lerpPitch = (double) pitch;
        this.lerpSteps = 10;
    }

    private void lerpTick()
    {
        if(this.lerpSteps > 0)
        {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double) this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double) this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double) this.lerpSteps;
            double d3 = MathHelper.wrapDegrees(this.lerpYaw - (double) this.getYRot());
            --this.lerpSteps;
            this.setPos(d0, d1, d2);

            float y = (float) ((double) this.getYRot() + d3 / (double) this.lerpSteps);
            float x = (float) ((double) this.getXRot() + (this.lerpPitch - (double) this.getXRot()) / (double) this.lerpSteps);
            if (!Float.isNaN(y)) this.setYRot(y);
            if (!Float.isNaN(x)) this.setXRot(x);
        }
    }

    @Override
    public void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        if(this.isControlledByLocalInstance() && this.lerpSteps > 0)
        {
            this.lerpSteps = 0;
            this.setPos(this.lerpX, this.lerpY, this.lerpZ);
            this.setYRot( (float) this.lerpYaw);
            this.setXRot((float) this.lerpPitch);
        }

        // This probably could be removed in favour of the updatePassengerPosition method
        passenger.setXRot(this.getXRot());
        passenger.setYRot(this.getYRot());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return this.getPassengers().size() < Seats.length;
    }

    // Come on you can't just push a vehicle, you're not *that* strong
    @Override
    public void push(double x, double y, double z) {}

    protected long lastTick;
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

                if ((inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f) != throttle) PacketHandler.INSTANCE.sendToServer(new MessageThrottle(inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f));
                if ((inputLeft ? -1f : inputRight ? 1f : 0f) != steeringInput) PacketHandler.INSTANCE.sendToServer(new MessageSteering(inputLeft ? -1f : inputRight ? 1f : 0f));
                if ((inputJump ? 1f : 0f) != handbrake) PacketHandler.INSTANCE.sendToServer(new MessageHandbrake(inputJump ? 1f : 0f));
                this.setThrottle(inputForward && inputBackward ? 2f : inputForward ? 1f : inputBackward ? -1f : 0f);
                this.setSteering(inputLeft ? -1f : inputRight ? 1f : 0f);
                this.setHandbrake(inputJump ? 1f : 0f);

//                UpdateCamera(deltaTime);
            }
        }
    }

    protected void UpdateCamera(double deltaTime) {
        Entity camera = Minecraft.getInstance().cameraEntity;

        float xPos = Maths.fInterpToExp((float) camera.getX(), (float) this.getX(), 3f, (float) deltaTime);
        float yPos = Maths.fInterpToExp((float) camera.getY(), (float) this.getY(), 3f, (float) deltaTime);
        float zPos = Maths.fInterpToExp((float) camera.getZ(), (float) this.getZ(), 3f, (float) deltaTime);

        camera.setPos(xPos, yPos, zPos);
        camera.setYRot(Maths.fInterpToExp(camera.getYRot(),this.getYRot(), 3f, (float) deltaTime));
    }

    protected abstract void setupSeats();
    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYaw;
    protected double lerpPitch;

    @Override
    protected boolean canRide(@NotNull Entity entity)
    {
        return SeatManager.contains(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!player.level().isClientSide && !player.isCrouching())
        {
            // Unused for now
            ItemStack heldItem = player.getItemInHand(hand);

            if(this.canRide(player))
            {
                if (SeatManager.contains(player.getUUID())) return InteractionResult.SUCCESS;
                int closestSeatIndex = -1;
                double closestDistance = 0;
                for (int i = 0; i < Seats.length; i++) {
                    SeatData seat = Seats[i];
                    if (SeatManager.get(i) == UUID.fromString("00000000-0000-0000-0000-000000000000")) continue;

                    Vec3 seatVec = seat.seatOffset.yRot(-this.getYRot() * ((float)Math.PI / 180F)).add(this.position());
                    double distance = player.distanceToSqr(seatVec.x, seatVec.y - player.getBbHeight() / 2F, seatVec.z);
                    if(closestSeatIndex == -1 || distance < closestDistance)
                    {
                        closestSeatIndex = i;
                        closestDistance = distance;
                    }
                };
                LOGGER.info("Seat info: " + String.valueOf(closestSeatIndex) + " - " + player.getName() + " - " + (player.level().isClientSide ? "Client" : "Server"));
                LOGGER.info("Chosen 'closest' seat: " + String.valueOf(closestSeatIndex) + " with distance of " + String.valueOf(closestDistance) + " metres");
                SeatManager.set(closestSeatIndex, player.getUUID());
                this.entityData.set(SEAT_TRACKER, writeSeatManager(), true);
                if (closestSeatIndex != -1) player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        //LOGGER.info(SeatManager.toString() + " - " + (player.level().isClientSide ? "Client" : "Server"));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void positionRider(Entity rider_, MoveFunction moveFunc) {
        super.positionRider(rider_, moveFunc);
        updatePassengerPosition(rider_);
    }

    protected void updatePassengerPosition(Entity passenger)
    {
        if(passenger.level().isClientSide() && this.hasPassenger(passenger)) {
            if (!SeatManager.contains(passenger.getUUID())) return;
            SeatData seat = Seats[SeatManager.indexOf(passenger.getUUID())];

            if (seat == null) return;
            passenger.setYBodyRot(this.getYRot() + seat.yawOffset);
            Vec3 position = new Vec3(this.position().x, this.position().y, this.position().z).add(seat.seatOffset.yRot(-this.getYRot() * ((float)Math.PI / 180F)));
            //position = position.yRot((this.getYRot()));
            passenger.setPos(position);
        }
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (!entity.level().isClientSide && SeatManager.contains(entity.getUUID())) {
            if (SeatManager.indexOf(entity.getUUID()) == 0) throttle = 0;
            SeatManager.set(SeatManager.indexOf(entity.getUUID()), UUID.fromString("00000000-0000-0000-0000-000000000000"));
            this.entityData.set(SEAT_TRACKER, writeSeatManager());
        }
        LOGGER.info(SeatManager.toString() + " - " + (this.level().isClientSide ? "Client" : "Server"));
    }

    // Overriding the move method, because 'collide' is private
    // 99% of this is unchanged, except for the 'this.collide()' call
//    @Override
//    public final void move(@NotNull MoverType moverType, @NotNull Vec3 motion) {
//        if (this.noPhysics) {
//            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
//        } else {
//            this.wasOnFire = this.isOnFire();
//            if (moverType == MoverType.PISTON) {
//                motion = this.limitPistonMovement(motion);
//                if (motion.equals(Vec3.ZERO)) {
//                    return;
//                }
//            }
//
//            this.level().getProfiler().push("move");
//            if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7D) {
//                motion = motion.multiply(this.stuckSpeedMultiplier);
//                this.stuckSpeedMultiplier = Vec3.ZERO;
//                this.setDeltaMovement(Vec3.ZERO);
//            }
//
//            motion = this.maybeBackOffFromEdge(motion, moverType);
//            Vec3 vec3 = this.doCollide(motion);
//            double d0 = vec3.lengthSqr();
//            if (d0 > 1.0E-7D) {
//                if (this.fallDistance != 0.0F && d0 >= 1.0D) {
//                    BlockHitResult blockhitresult = this.level().clip(new ClipContext(this.position(), this.position().add(vec3), ClipContext.Block.FALLDAMAGE_RESETTING, ClipContext.Fluid.WATER, this));
//                    if (blockhitresult.getType() != HitResult.Type.MISS) {
//                        this.resetFallDistance();
//                    }
//                }
//
//                this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
//            }
//
//            this.level().getProfiler().pop();
//            this.level().getProfiler().push("rest");
//            boolean flag4 = !Mth.equal(motion.x, vec3.x);
//            boolean flag = !Mth.equal(motion.z, vec3.z);
//            this.horizontalCollision = flag4 || flag;
//            this.verticalCollision = motion.y != vec3.y;
//            this.verticalCollisionBelow = this.verticalCollision && motion.y < 0.0D;
//            if (this.horizontalCollision) {
//                this.minorHorizontalCollision = this.isHorizontalCollisionMinor(vec3);
//            } else {
//                this.minorHorizontalCollision = false;
//            }
//
//            this.setOnGroundWithKnownMovement(this.verticalCollisionBelow, vec3);
//            BlockPos blockpos = this.getOnPosLegacy();
//            BlockState blockstate = this.level().getBlockState(blockpos);
//            this.checkFallDamage(vec3.y, this.onGround(), blockstate, blockpos);
//            if (this.isRemoved()) {
//                this.level().getProfiler().pop();
//            } else {
//                if (this.horizontalCollision) {
//                    Vec3 vec31 = this.getDeltaMovement();
//                    this.setDeltaMovement(flag4 ? 0.0D : vec31.x, vec31.y, flag ? 0.0D : vec31.z);
//                }
//
//                Block block = blockstate.getBlock();
//                if (motion.y != vec3.y) {
//                    block.updateEntityAfterFallOn(this.level(), this);
//                }
//
//                if (this.onGround()) {
//                    block.stepOn(this.level(), blockpos, blockstate, this);
//                }
//
//                Entity.MovementEmission entity$movementemission = this.getMovementEmission();
//                if (entity$movementemission.emitsAnything() && !this.isPassenger()) {
//                    double d1 = vec3.x;
//                    double d2 = vec3.y;
//                    double d3 = vec3.z;
//                    this.flyDist = (float)((double)this.flyDist + vec3.length() * 0.6D);
//                    BlockPos blockPos1 = this.getOnPos();
//                    BlockState blockState1 = this.level().getBlockState(blockPos1);
//                    d2 = 0.0D;
//
//                    this.walkDist += (float)vec3.horizontalDistance() * 0.6F;
//                    this.moveDist += (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3) * 0.6F;
//                    if (this.moveDist > this.nextStep && !blockState1.isAir()) {
//                       this.nextStep = this.nextStep();
//                    }
//                }
//
//                this.tryCheckInsideBlocks();
//                float f = this.getBlockSpeedFactor();
//                this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 1.0D, (double)f));
//                if (this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6D)).noneMatch((p_20127_) -> {
//                    return p_20127_.is(BlockTags.FIRE) || p_20127_.is(Blocks.LAVA);
//                })) {
//                    if (this.getRemainingFireTicks() <= 0) {
//                        this.setRemainingFireTicks(-this.getFireImmuneTicks());
//                    }
//
//                    if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble() || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType)))) {
//                        this.playEntityOnFireExtinguishedSound();
//                    }
//                }
//
//                if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble() || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType)))) {
//                    this.setRemainingFireTicks(-this.getFireImmuneTicks());
//                }
//
//                this.level().getProfiler().pop();
//            }
//        }
//    }

    private Vec3 doCollide(Vec3 motion) {
        double d1 = motion.x;
        double d2 = motion.y;
        double d3 = motion.z;
        for (Vec3[] offset : collision) {
            Vec3 start = offset[0].yRot(-this.getYRot() * (Mth.PI/180));
            Vec3 end = offset[1].yRot(-this.getYRot() * (Mth.PI/180));
            AABB aabb = new AABB(this.getX() + start.x, this.getY() + start.y, this.getZ() + start.z, this.getX() + end.x, this.getY() + end.y, this.getZ() + end.z);
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
            Vec3 vec31 = collideBoundingBox(this, new Vec3(motion.x, (double)stepHeight, motion.z), aabb, this.level(), list);
            Vec3 vec32 = collideBoundingBox(this, new Vec3(0.0D, (double)stepHeight, 0.0D), aabb.expandTowards(motion.x, 0.0D, motion.z), this.level(), list);
            if (vec32.y < (double)stepHeight) {
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

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Default", 5, this::animController));
    }

    protected <E extends AbstractVehicle> PlayState animController(final AnimationState<E> event) {
        return event.setAndContinue(ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

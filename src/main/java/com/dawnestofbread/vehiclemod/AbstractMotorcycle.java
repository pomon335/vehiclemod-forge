package com.dawnestofbread.vehiclemod;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Date;

public abstract class AbstractMotorcycle extends WheeledVehicle {
    protected double wheelieAngularVelocity;
    protected double wheelieAngularAcceleration;
    protected AbstractMotorcycle(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    @Override
    public void tick() {
        double deltaTime = (double) (new Date().getTime() - lastTick) / 1000;
        super.tick();
        if (!this.level().isClientSide) {
            wheelieAngularAcceleration = sprint * (Mth.PI / 45);
            if (forwardSpeed * movementDirection > 0 && sprint > 0)  {
                wheelieAngularVelocity += wheelieAngularAcceleration;
            } else {
                if (this.getXRot() - (wheelieAngularVelocity * deltaTime) > 0) wheelieAngularVelocity -= .1;
            }
            this.turn(0f, wheelieAngularVelocity * deltaTime);
        }
    }
}

package com.dawnestofbread.vehiclemod.utils;


import net.minecraft.world.phys.Vec3;

public class WheelData {
    public Vec3 startingRelativePosition;
    public boolean affectedBySteering;
    public boolean affectedByEngine;
    public boolean affectedByBrake;
    public boolean affectedByHandbrake;
    public double radius;
    public double width;
    public double mass;
    public double angularVelocity = 0;
    public double xRot = 0;
    private double yRot = 0;

    public double getXRot() {
        return xRot;
    }

    public void setXRot(double xRot) {
        this.xRot = xRot;
    }

    public double getYRot() {
        return yRot;
    }

    public void setYRot(double yRot) {
        this.yRot = yRot;
    }

    public boolean onGround;
    public double springMaxLength; // The max suspension length in metres
    public double springMinLength; // The min suspension length in metres (above resting position)
    public Vec3 currentRelativePosition = Vec3.ZERO;
    public double springLength;
    public Vec3 targetWorldPosition = Vec3.ZERO;
}

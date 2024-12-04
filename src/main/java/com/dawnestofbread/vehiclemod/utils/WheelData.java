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
    public boolean onGround;
    public double suspensionDrop; // The max suspension length in metres
    public double suspensionRaise; // The min suspension length in metres
    public Vec3 currentRelativePosition = Vec3.ZERO;
    public double currentSuspensionLength;
}

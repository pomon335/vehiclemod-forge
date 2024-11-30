package com.dawnestofbread.vehiclemod.utils;


import net.minecraft.world.phys.Vec3;

public class WheelData {
    public Vec3 startingRelativePosition;
    public boolean affectedByTurn;
    public boolean affectedByEngine;
    public boolean affectedByBrake;
    public boolean affectedByHandbrake;
    public double radius;
    public double mass;
    public double angularVelocity = 0;
    public double torque = 0;
    public double normalForce = 0;
    public double engineForce = 0;
    public double brakeForce = 0;
    public boolean onGround;
    public double frictionCoefficient;
    public double frictionLimit;
    public double totalForce;
    public double suspensionDrop; // The min suspension length
    public double suspensionRaise; // The max suspension length
    public Vec3 currentRelativePosition;
    public double currentFriction;
    public double currentRotation;
    public double currentTurnAngle;
    public double currentSuspensionLength;
    public boolean visible;
}

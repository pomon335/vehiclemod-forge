package com.dawnestofbread.vehiclemod.utils;

import net.minecraft.world.phys.Vec3;

public class HitResult {
    public double distance;
    public net.minecraft.world.phys.HitResult.Type type;
    public boolean hit;
    public boolean inside;
    public Vec3 start;
    public Vec3 end;
    public Vec3 setEnd;
}

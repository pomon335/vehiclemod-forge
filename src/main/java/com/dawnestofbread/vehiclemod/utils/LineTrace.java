package com.dawnestofbread.vehiclemod.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LineTrace {
    public static HitResult lineTraceByType(Vec3 start, Vec3 end, ClipContext.Block blockResponse, ClipContext.Fluid fluidResponse, Entity caller) {
        ClipContext lineTrace = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caller);
        BlockHitResult r = caller.level().clip(lineTrace);
        HitResult result = new HitResult();
        result.type = r.getType();
        result.hit = r.getType() != net.minecraft.world.phys.HitResult.Type.MISS;
        result.distance = r.getLocation().distanceTo(start);
        result.inside = r.isInside();
        result.start = start;
        result.setEnd = end;
        result.end = r.getLocation();
        return result;
    }
}

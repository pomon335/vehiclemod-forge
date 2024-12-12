package com.dawnestofbread.vehiclemod.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class VectorUtils {
    public static double vectorMagnitude(Vec3 vectorIn) {
        return Math.sqrt(
                Math.pow(vectorIn.x, 2d) +
                Math.pow(vectorIn.y, 2d) +
                Math.pow(vectorIn.z, 2d)
        );
    }

    public static Vec3 divideVectorByScalar(Vec3 vectorToDivide, double doubleIn) {
        return new Vec3(vectorToDivide.x / doubleIn, vectorToDivide.y / doubleIn, vectorToDivide.z / doubleIn);
    }
    public static Vec3 rotateVectorToEntitySpace(Vec3 vectorIn, Entity entity) {
        return vectorIn.yRot(-entity.getYRot() * ((float)Math.PI / 180F)).xRot(-entity.getXRot() * ((float)Math.PI / 180F));
    }
    public static Vec3 rotateVectorToEntitySpaceYOnly(Vec3 vectorIn, Entity entity) {
        return vectorIn.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
    }
    public static Vec3 rotateVectorToEntitySpaceXOnly(Vec3 vectorIn, Entity entity) {
        return vectorIn.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
    }
}

package com.bread.vehiclemod.utils;

import net.minecraft.world.entity.Entity;

public class DebugUtils {
    public static String getInstanceSideName(Entity entity) {
        boolean isClient = entity.level().isClientSide;
        return isClient ? " Client " : " Server ";
    }
}

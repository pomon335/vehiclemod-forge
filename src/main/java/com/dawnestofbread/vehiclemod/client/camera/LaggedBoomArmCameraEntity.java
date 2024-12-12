package com.dawnestofbread.vehiclemod.client.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LaggedBoomArmCameraEntity extends Camera {
    final LocalPlayer target;

    public LaggedBoomArmCameraEntity(LocalPlayer target) {
        super();
        this.target = target;
    }
}

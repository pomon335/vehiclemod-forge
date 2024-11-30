package com.dawnestofbread.vehiclemod.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SurfaceHelper {
    public static BlockState getSurfaceFromPosition(BlockPos position) {
        return Minecraft.getInstance().level.getBlockState(position);
    }
    public static ParticleOptions getFrictionEffectBasedOnSurface(BlockState s) {
        Block surface = s.getBlock();

        return (s.is(BlockTags.TERRACOTTA)) ?
                (null) :
                (new BlockParticleOption(ParticleTypes.BLOCK, s));
    }

    public static ParticleOptions getSkidEffectBasedOnSurface(BlockState s) {
        Block surface = s.getBlock();

        return (s.is(BlockTags.TERRACOTTA)) ?
                (ParticleTypes.CAMPFIRE_COSY_SMOKE) :
                (new BlockParticleOption(ParticleTypes.BLOCK, s));
    }
    public static void spawnFrictionEffect(BlockState surface, Vec3 p, Vec3 vel) {
        if (getFrictionEffectBasedOnSurface(surface) != null) Minecraft.getInstance().level.addParticle(getFrictionEffectBasedOnSurface(surface), p.x, p.y, p.z, vel.x, vel.y, vel.z);
    }
    public static void spawnSkidEffect(BlockState surface, Vec3 p, Vec3 vel) {
        if (getSkidEffectBasedOnSurface(surface) != null) Minecraft.getInstance().level.addParticle(getSkidEffectBasedOnSurface(surface), p.x, p.y, p.z, vel.x, vel.y, vel.z);
    }
}
